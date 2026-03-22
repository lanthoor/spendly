package `in`.co.spendly.utils.parsers

import `in`.co.spendly.utils.TransactionType

/**
 * Parser for ICICI Bank SMS.
 */
class IciciParser : BaseBankParser() {

    override fun extractAccountHint(smsBody: String): String? {
        val iciciPattern = Regex("""A/C\s*XX(\d{4})""", RegexOption.IGNORE_CASE)
        return iciciPattern.find(smsBody)?.groupValues?.get(1)
            ?: super.extractAccountHint(smsBody)
    }

    override fun extractMerchant(smsBody: String): String? {
        val iciciPattern = Regex("""for\s+(?:UPI/)?(.+?)(?:\.|$)""", RegexOption.IGNORE_CASE)
        return iciciPattern.find(smsBody)?.groupValues?.get(1)?.trim()
            ?: super.extractMerchant(smsBody)
    }

    override fun extractDate(smsBody: String): Long? {
        val iciciPattern = Regex("""(\d{1,2})-([A-Za-z]{3})-(\d{2})""")
        return iciciPattern.find(smsBody)?.let { parseDateFromMatch(it) }
            ?: super.extractDate(smsBody)
    }

    override fun getDefaultDescription(type: TransactionType?): String {
        return when (type) {
            TransactionType.EXPENSE -> "ICICI Debit Transaction"
            TransactionType.INCOME -> "ICICI Credit Transaction"
            null -> "ICICI Transaction"
        }
    }
}
