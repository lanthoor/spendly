package dev.lanthoor.spendly.utils.parsers

import dev.lanthoor.spendly.utils.TransactionType

/**
 * Parser for UPI transaction SMS.
 */
class UpiParser : BaseBankParser() {

    override fun extractTransactionType(smsBody: String): TransactionType? {
        return when {
            smsBody.contains("sent", ignoreCase = true) -> TransactionType.EXPENSE
            smsBody.contains("paid", ignoreCase = true) -> TransactionType.EXPENSE
            smsBody.contains("received", ignoreCase = true) -> TransactionType.INCOME
            else -> super.extractTransactionType(smsBody)
        }
    }

    override fun extractMerchant(smsBody: String): String? {
        val upiIdPattern = Regex("""(?:to|from)\s+([\w.-]+@[\w.-]+)""", RegexOption.IGNORE_CASE)
        return upiIdPattern.find(smsBody)?.groupValues?.get(1)
            ?: super.extractMerchant(smsBody)
    }

    override fun getDefaultDescription(type: TransactionType?): String {
        return when (type) {
            TransactionType.EXPENSE -> "UPI Payment"
            TransactionType.INCOME -> "UPI Receipt"
            null -> "UPI Transaction"
        }
    }
}
