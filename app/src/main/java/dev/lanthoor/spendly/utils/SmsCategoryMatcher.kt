package dev.lanthoor.spendly.utils

import java.util.Locale

data class SmsCategoryMatch(
    val categoryName: String,
    val score: Float,
    val reason: String
)

object SmsCategoryMatcher {
    private const val MIN_SCORE = 3f

    private data class KeywordRule(
        val categoryName: String,
        val keywords: List<String>
    )

    private val expenseRules = listOf(
        KeywordRule("Food", listOf("swiggy", "zomato", "restaurant", "cafe", "dining")),
        KeywordRule("Groceries", listOf("mart", "supermarket", "grocery", "dmart", "bigbasket", "zepto", "blinkit")),
        KeywordRule("Travel", listOf("uber", "ola", "irctc", "metro", "fuel", "petrol", "diesel", "toll")),
        KeywordRule("Utilities", listOf("electricity", "water bill", "broadband", "recharge", "postpaid", "dth", "gas bill")),
        KeywordRule("Shopping", listOf("amazon", "flipkart", "myntra", "ajio", "meesho")),
        KeywordRule("Healthcare", listOf("pharmacy", "hospital", "clinic", "medic", "apollo", "diagnostic")),
        KeywordRule("Rent", listOf("rent", "landlord", "lease")),
        KeywordRule("Education", listOf("school", "college", "tuition", "course", "udemy", "byju")),
        KeywordRule("Media", listOf("netflix", "spotify", "prime video", "youtube", "hotstar"))
    )

    private val incomeRules = listOf(
        KeywordRule("Salary", listOf("salary", "payroll", "employer", "wages", "salary credited")),
        KeywordRule("Interest", listOf("interest credited", "int payout", "interest payout", "interest")),
        KeywordRule("Refund", listOf("refund", "reversal", "chargeback", "refund initiated")),
        KeywordRule("Bonus", listOf("bonus", "incentive", "ex gratia")),
        KeywordRule("Freelance", listOf("invoice", "client payment", "consulting", "freelance", "project fee")),
        KeywordRule("Business", listOf("business", "vendor payment", "settlement", "merchant settlement"))
    )

    fun match(
        parsed: ParsedTransaction,
        smsBody: String,
        sender: String
    ): SmsCategoryMatch? {
        val merchantBlob = normalize(listOfNotNull(parsed.merchantName))
        val descriptionBlob = normalize(listOf(parsed.description))
        val bodyBlob = normalize(listOf(smsBody, sender))
        val rules = when (parsed.transactionType) {
            TransactionType.EXPENSE -> expenseRules
            TransactionType.INCOME -> incomeRules
        }

        val scored = rules.map { rule ->
            val hits = mutableListOf<String>()
            var score = 0f
            var merchantHits = 0
            rule.keywords.forEach { keyword ->
                if (containsKeyword(merchantBlob, keyword)) {
                    score += 5f
                    merchantHits++
                    hits += "merchant:$keyword"
                } else if (containsKeyword(descriptionBlob, keyword)) {
                    score += 3f
                    hits += "description:$keyword"
                } else if (containsKeyword(bodyBlob, keyword)) {
                    score += 2f
                    hits += "body:$keyword"
                }
            }
            MatchScore(rule.categoryName, score, merchantHits, hits)
        }.filter { it.score > 0f }

        if (scored.isEmpty()) return null

        val best = scored.maxWithOrNull(
            compareBy<MatchScore> { it.score }
                .thenBy { it.merchantHits }
        ) ?: return null
        val secondBestScore = scored
            .asSequence()
            .filter { it.categoryName != best.categoryName }
            .maxOfOrNull { it.score }
            ?: 0f

        if (best.score < MIN_SCORE) return null
        if (best.score <= secondBestScore) return null

        val reason = best.hits.take(2).joinToString(", ")
        return SmsCategoryMatch(
            categoryName = best.categoryName,
            score = best.score,
            reason = reason
        )
    }

    private data class MatchScore(
        val categoryName: String,
        val score: Float,
        val merchantHits: Int,
        val hits: List<String>
    )

    private fun normalize(parts: List<String>): String {
        return parts
            .joinToString(" ")
            .lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun containsKeyword(blob: String, rawKeyword: String): Boolean {
        if (blob.isBlank()) return false
        val keyword = rawKeyword
            .lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
        if (keyword.isBlank()) return false

        val pattern = if (keyword.contains(' ')) {
            "\\b" + keyword
                .split(" ")
                .joinToString("\\\\s+") { Regex.escape(it) } + "\\b"
        } else {
            "\\b${Regex.escape(keyword)}\\b"
        }
        return Regex(pattern).containsMatchIn(blob)
    }
}
