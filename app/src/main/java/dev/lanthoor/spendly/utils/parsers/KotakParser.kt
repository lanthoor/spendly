package dev.lanthoor.spendly.utils.parsers

import dev.lanthoor.spendly.utils.TransactionType

/**
 * Parser for Kotak Bank SMS.
 */
class KotakParser : BaseBankParser() {

    override fun extractAccountHint(smsBody: String): String? {
        val kotakPattern = Regex("""A/c\s*\*+(\d{4})""", RegexOption.IGNORE_CASE)
        return kotakPattern.find(smsBody)?.groupValues?.get(1)
            ?: super.extractAccountHint(smsBody)
    }

    override fun extractDate(smsBody: String): Long? {
        val kotakPattern = Regex("""(\d{1,2})-([A-Za-z]{3})-(\d{2})""")
        return kotakPattern.find(smsBody)?.let { parseDateFromMatch(it) }
            ?: super.extractDate(smsBody)
    }

    override fun getDefaultDescription(type: TransactionType?): String {
        return when (type) {
            TransactionType.EXPENSE -> "Kotak Debit Transaction"
            TransactionType.INCOME -> "Kotak Credit Transaction"
            null -> "Kotak Transaction"
        }
    }
}
