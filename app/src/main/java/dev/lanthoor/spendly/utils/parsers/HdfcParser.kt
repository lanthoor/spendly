package dev.lanthoor.spendly.utils.parsers

import dev.lanthoor.spendly.core.model.finance.TransactionType

/**
 * Parser for HDFC Bank SMS.
 */
class HdfcParser : BaseBankParser() {

    override fun extractAccountHint(smsBody: String): String? {
        val hdfcPattern = Regex("""(?:A/c|Card)\s*\*+(\d{4})""", RegexOption.IGNORE_CASE)
        return hdfcPattern.find(smsBody)?.groupValues?.get(1)
            ?: super.extractAccountHint(smsBody)
    }

    override fun extractMerchant(smsBody: String): String? {
        val hdfcPattern = Regex("""at\s+([A-Z][A-Z\s]+?)(?:\s+on|\.)""", RegexOption.IGNORE_CASE)
        return hdfcPattern.find(smsBody)?.groupValues?.get(1)?.trim()
            ?: super.extractMerchant(smsBody)
    }

    override fun extractDate(smsBody: String, smsTimestamp: Long): Long? {
        val hdfcPattern = Regex("""(\d{1,2})-([A-Za-z]{3})-(\d{2})""")
        return hdfcPattern.find(smsBody)?.let { parseDateFromMatch(it, smsTimestamp) }
            ?: super.extractDate(smsBody, smsTimestamp)
    }

    override fun getDefaultDescription(type: TransactionType?): String {
        return when (type) {
            TransactionType.EXPENSE -> "HDFC Debit Transaction"
            TransactionType.INCOME -> "HDFC Credit Transaction"
            null -> "HDFC Transaction"
        }
    }
}
