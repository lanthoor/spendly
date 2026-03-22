package dev.lanthoor.spendly.utils.parsers

import dev.lanthoor.spendly.utils.TransactionType

/**
 * Parser for Scapia Federal Bank SMS.
 */
class ScapiaParser : BaseBankParser() {

    override fun extractAccountHint(smsBody: String): String? {
        val accountPattern = Regex("""(?:Scapia Federal\s*(Visa|RuPay))""", RegexOption.IGNORE_CASE)
        return accountPattern.find(smsBody)?.groupValues?.get(1)
            ?: super.extractAccountHint(smsBody)
    }

    override fun extractMerchant(smsBody: String): String? {
        val merchantPattern = Regex("""at\s+([A-Z][A-Z\s]+?)on your""", RegexOption.IGNORE_CASE)
        return merchantPattern.find(smsBody)?.groupValues?.get(1)?.trim()
            ?: super.extractMerchant(smsBody)
    }

    override fun getDefaultDescription(type: TransactionType?): String {
        return when (type) {
            TransactionType.EXPENSE -> "Scapia Debit Transaction"
            TransactionType.INCOME -> "Scapia Credit Transaction"
            null -> "Scapia Transaction"
        }
    }
}
