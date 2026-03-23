package dev.lanthoor.spendly.utils

import dev.lanthoor.spendly.domain.model.Account

object SmsAccountMatcher {
    private const val HINT_SCORE = 100
    private const val KEYWORD_SCORE = 20
    private const val TYPE_AFFINITY_SCORE = 15

    private val bankAndProviderKeywords = setOf(
        "hdfc",
        "icici",
        "sbi",
        "axis",
        "kotak",
        "scapia",
        "federal",
        "paytm",
        "phonepe",
        "gpay",
        "googlepay",
        "upi"
    )

    private val cardIndicators = listOf(
        "credit card",
        "debit card",
        "card",
        "visa",
        "rupay",
        "mastercard"
    )

    private val walletIndicators = listOf(
        "@upi",
        "@paytm",
        "@phonepe",
        "@googlepay",
        "@gpay",
        "phonepe",
        "paytm",
        "googlepay",
        "gpay",
        "upi"
    )

    private val bankIndicators = listOf(
        "a/c",
        "acct",
        "account",
        "debited from account",
        "credited to account"
    )

    fun resolveAccountId(
        accounts: List<Account>,
        parsed: ParsedTransaction,
        sender: String,
        body: String
    ): Long? {
        if (accounts.isEmpty()) return null

        val normalizedSender = normalize(sender)
        val normalizedBody = normalize(body)
        val normalizedDescription = normalize(parsed.description)
        val normalizedMerchant = normalize(parsed.merchantName.orEmpty())
        val signalText = listOf(normalizedSender, normalizedBody)
            .filter { it.isNotBlank() }
            .joinToString(" ")
        val typeSignalText = listOf(signalText, normalizedDescription, normalizedMerchant)
            .filter { it.isNotBlank() }
            .joinToString(" ")

        val matchedKeywords = bankAndProviderKeywords.filter { keyword ->
            signalText.contains(keyword)
        }
        val preferredTypes = inferPreferredTypes(typeSignalText)

        val scored = accounts.map { account ->
            val accountName = normalize(account.name)
            var score = 0

            if (hasStrongHintMatch(accountName, parsed.accountHint)) {
                score += HINT_SCORE
            }

            matchedKeywords.forEach { keyword ->
                if (accountName.contains(keyword)) {
                    score += KEYWORD_SCORE
                }
            }

            if (preferredTypes.contains(account.type)) {
                score += TYPE_AFFINITY_SCORE
            }

            ScoredAccount(account, score)
        }

        val best = scored.sortedWith(
            compareByDescending<ScoredAccount> { it.score }
                .thenBy { it.account.sortOrder }
                .thenBy { it.account.id }
        ).firstOrNull() ?: return null

        return if (best.score > 0) best.account.id else null
    }

    private fun hasStrongHintMatch(accountName: String, accountHint: String?): Boolean {
        if (accountHint.isNullOrBlank()) return false
        val normalizedHint = accountHint.filter { it.isDigit() }
        if (normalizedHint.length < 4) return false

        return accountName.filter { it.isDigit() }.contains(normalizedHint)
    }

    private fun inferPreferredTypes(signalText: String): Set<AccountType> {
        val preferred = mutableSetOf<AccountType>()

        if (cardIndicators.any { signalText.contains(it) }) {
            preferred.add(AccountType.CARD)
        }
        if (walletIndicators.any { signalText.contains(it) }) {
            preferred.add(AccountType.WALLET)
        }
        if (bankIndicators.any { signalText.contains(it) }) {
            preferred.add(AccountType.BANK)
        }

        return preferred
    }

    private fun normalize(value: String): String {
        return value.lowercase()
            .replace("-", " ")
            .replace("_", " ")
            .replace("*", "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private data class ScoredAccount(
        val account: Account,
        val score: Int
    )
}
