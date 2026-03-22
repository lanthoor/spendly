package dev.lanthoor.spendly.utils.parsers

import dev.lanthoor.spendly.utils.TransactionType

/**
 * Parser for Axis Bank SMS.
 */
class AxisParser : BaseBankParser() {

    override fun extractAccountHint(smsBody: String): String? {
        val axisPattern = Regex("""(?:A/c|Card)\s*\*+(\d{4})""", RegexOption.IGNORE_CASE)
        return axisPattern.find(smsBody)?.groupValues?.get(1)
            ?: super.extractAccountHint(smsBody)
    }

    override fun extractMerchant(smsBody: String): String? {
        val axisPattern = Regex("""at\s+([A-Z][A-Z\s]+?)(?:\s+on|\s*$)""", RegexOption.IGNORE_CASE)
        return axisPattern.find(smsBody)?.groupValues?.get(1)?.trim()
            ?: super.extractMerchant(smsBody)
    }

    override fun extractDate(smsBody: String): Long? {
        val axisPattern = Regex("""(\d{1,2})-([A-Za-z]{3})-(\d{2})""")
        return axisPattern.find(smsBody)?.let { parseDateFromMatch(it) }
            ?: super.extractDate(smsBody)
    }

    override fun getDefaultDescription(type: TransactionType?): String {
        return when (type) {
            TransactionType.EXPENSE -> "Axis Debit Transaction"
            TransactionType.INCOME -> "Axis Credit Transaction"
            null -> "Axis Transaction"
        }
    }
}
