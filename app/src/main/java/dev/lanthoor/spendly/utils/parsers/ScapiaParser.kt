package dev.lanthoor.spendly.utils.parsers

import dev.lanthoor.spendly.core.model.finance.TransactionType

/**
 * Parser for Scapia Federal Bank SMS.
 */
class ScapiaParser : BaseBankParser() {

    override fun extractTransactionType(smsBody: String): TransactionType? {
        val hasCardContext = smsBody.contains("Scapia", ignoreCase = true) ||
            smsBody.contains("Federal", ignoreCase = true) ||
            smsBody.contains("card", ignoreCase = true) ||
            smsBody.contains("visa", ignoreCase = true) ||
            smsBody.contains("rupay", ignoreCase = true)
        val hasExpensePhrase = smsBody.contains("txn of", ignoreCase = true) ||
            smsBody.contains("transaction of", ignoreCase = true) ||
            smsBody.contains("spent", ignoreCase = true)

        if (hasCardContext && hasExpensePhrase) {
            return TransactionType.EXPENSE
        }

        return super.extractTransactionType(smsBody)
    }

    override fun extractAccountHint(smsBody: String): String? {
        val accountPattern = Regex("""(?:Scapia Federal\s*(Visa|RuPay))""", RegexOption.IGNORE_CASE)
        return accountPattern.find(smsBody)?.groupValues?.get(1)
            ?: super.extractAccountHint(smsBody)
    }

    override fun extractMerchant(smsBody: String): String? {
        val merchantPattern = Regex(
            """\bat\s+(.+?)\s+on\s+your\b""",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        )
        val cleanedMerchant = merchantPattern.find(smsBody)?.groupValues?.get(1)
            ?.replace("\\s+".toRegex(), " ")
            ?.trim(' ', '.', ',', ';', ':', '-', '_')
            ?.takeIf { it.isNotBlank() }

        return cleanedMerchant
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
