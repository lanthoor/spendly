package dev.lanthoor.spendly.utils.parsers

import dev.lanthoor.spendly.utils.TransactionType

/**
 * Parser for SBI (State Bank of India) SMS.
 */
class SbiParser : BaseBankParser() {

    override fun extractAccountHint(smsBody: String): String? {
        val sbiPattern = Regex("""Acct\s*XX(\d{4})""", RegexOption.IGNORE_CASE)
        return sbiPattern.find(smsBody)?.groupValues?.get(1)
            ?: super.extractAccountHint(smsBody)
    }

    override fun extractDate(smsBody: String, smsTimestamp: Long): Long? {
        val sbiPattern = Regex("""(\d{1,2})([A-Za-z]{3})(\d{2})""")
        return sbiPattern.find(smsBody)?.let { parseDateFromMatch(it, smsTimestamp) }
            ?: super.extractDate(smsBody, smsTimestamp)
    }

    override fun getDefaultDescription(type: TransactionType?): String {
        return when (type) {
            TransactionType.EXPENSE -> "SBI Debit Transaction"
            TransactionType.INCOME -> "SBI Credit Transaction"
            null -> "SBI Transaction"
        }
    }
}
