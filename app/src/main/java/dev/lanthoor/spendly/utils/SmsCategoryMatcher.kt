package dev.lanthoor.spendly.utils

import dev.lanthoor.spendly.domain.model.Category
import java.util.Locale

data class SmsCategoryMatch(
    val categoryName: String,
    val score: Float,
    val reason: String
)

data class SmsCategoryResolution(
    val category: Category?,
    val matchedCategory: SmsCategoryMatch?,
    val reason: String,
    val usedFallback: Boolean
)

object SmsCategoryMatcher {
    private const val MIN_SCORE = 3f
    private const val OTHERS_CATEGORY_NAME = "Others"
    private val NON_ALPHANUMERIC_REGEX = Regex("[^a-z0-9\\s]")
    private val MULTI_SPACE_REGEX = Regex("\\s+")
    private val SCORE_COMPARATOR = compareBy<MatchScore> { it.score }
        .thenBy { it.merchantHits }

    private data class KeywordRule(
        val categoryName: String,
        val patterns: List<KeywordPattern>
    )

    private data class KeywordPattern(
        val keyword: String,
        val regex: Regex
    )

    private val expenseRules = listOf(
        keywordRule("Food", listOf("swiggy", "zomato", "restaurant", "cafe", "dining")),
        keywordRule("Groceries", listOf("mart", "supermarket", "grocery", "dmart", "bigbasket", "zepto", "blinkit")),
        keywordRule("Travel", listOf("uber", "ola", "irctc", "metro", "fuel", "petrol", "diesel", "toll")),
        keywordRule("Utilities", listOf("electricity", "water bill", "broadband", "recharge", "postpaid", "dth", "gas bill")),
        keywordRule("Shopping", listOf("amazon", "flipkart", "myntra", "ajio", "meesho")),
        keywordRule("Healthcare", listOf("pharmacy", "hospital", "clinic", "medic", "apollo", "diagnostic")),
        keywordRule("Rent", listOf("rent", "landlord", "lease")),
        keywordRule("Education", listOf("school", "college", "tuition", "course", "udemy", "byju")),
        keywordRule("Media", listOf("netflix", "spotify", "prime video", "youtube", "hotstar"))
    )

    private val incomeRules = listOf(
        keywordRule("Salary", listOf("salary", "payroll", "employer", "wages", "salary credited")),
        keywordRule("Interest", listOf("interest credited", "int payout", "interest payout", "interest")),
        keywordRule("Refund", listOf("refund", "reversal", "chargeback", "refund initiated")),
        keywordRule("Bonus", listOf("bonus", "incentive", "ex gratia")),
        keywordRule("Freelance", listOf("invoice", "client payment", "consulting", "freelance", "project fee")),
        keywordRule("Business", listOf("business", "vendor payment", "settlement", "merchant settlement"))
    )

    fun buildCategoryLookup(categories: List<Category>): Map<String, Category> {
        return categories.associateBy { normalizeCategoryName(it.name) }
    }

    fun resolveCategory(
        parsed: ParsedTransaction,
        smsBody: String,
        sender: String,
        categoryLookup: Map<String, Category>
    ): SmsCategoryResolution {
        val matchedCategory = match(parsed, smsBody, sender)
        val resolvedFromMatch = matchedCategory?.let {
            categoryLookup[normalizeCategoryName(it.categoryName)]
        }
        val fallbackCategory = categoryLookup[normalizeCategoryName(OTHERS_CATEGORY_NAME)]
        val resolvedCategory = resolvedFromMatch ?: fallbackCategory
        val reason = when {
            matchedCategory == null && fallbackCategory != null -> "fallback:others"
            matchedCategory == null -> "fallback:missing:others"
            resolvedFromMatch == null -> {
                "fallback:missing:${matchedCategory.categoryName.lowercase(Locale.ROOT)}"
            }

            else -> matchedCategory.reason
        }

        return SmsCategoryResolution(
            category = resolvedCategory,
            matchedCategory = matchedCategory,
            reason = reason,
            usedFallback = resolvedFromMatch == null
        )
    }

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
            rule.patterns.forEach { pattern ->
                if (containsKeyword(merchantBlob, pattern)) {
                    score += 5f
                    merchantHits++
                    hits += "merchant:${pattern.keyword}"
                } else if (containsKeyword(descriptionBlob, pattern)) {
                    score += 3f
                    hits += "description:${pattern.keyword}"
                } else if (containsKeyword(bodyBlob, pattern)) {
                    score += 2f
                    hits += "body:${pattern.keyword}"
                }
            }
            MatchScore(rule.categoryName, score, merchantHits, hits)
        }.filter { it.score > 0f }

        if (scored.isEmpty()) return null

        val best = scored.maxWithOrNull(SCORE_COMPARATOR) ?: return null
        val secondBest = scored
            .asSequence()
            .filter { it.categoryName != best.categoryName }
            .maxWithOrNull(SCORE_COMPARATOR)

        if (best.score < MIN_SCORE) return null
        if (secondBest != null && SCORE_COMPARATOR.compare(best, secondBest) <= 0) return null

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

    private fun keywordRule(categoryName: String, keywords: List<String>): KeywordRule {
        return KeywordRule(
            categoryName = categoryName,
            patterns = keywords.map { keyword ->
                KeywordPattern(keyword = keyword, regex = buildKeywordRegex(keyword))
            }
        )
    }

    private fun buildKeywordRegex(rawKeyword: String): Regex {
        val keyword = normalizeText(rawKeyword)
        if (keyword.isBlank()) return Regex("(?!x)x")
        val pattern = if (keyword.contains(' ')) {
            "\\b" + keyword
                .split(" ")
                .joinToString("\\\\s+") { Regex.escape(it) } + "\\b"
        } else {
            "\\b${Regex.escape(keyword)}\\b"
        }
        return Regex(pattern)
    }

    private fun normalize(parts: List<String>): String {
        return parts
            .joinToString(" ")
            .let(::normalizeText)
    }

    private fun normalizeText(input: String): String {
        return input
            .lowercase(Locale.ROOT)
            .replace(NON_ALPHANUMERIC_REGEX, " ")
            .replace(MULTI_SPACE_REGEX, " ")
            .trim()
    }

    private fun normalizeCategoryName(input: String): String {
        return input.trim().lowercase(Locale.ROOT)
    }

    private fun containsKeyword(blob: String, keywordPattern: KeywordPattern): Boolean {
        if (blob.isBlank()) return false
        return keywordPattern.regex.containsMatchIn(blob)
    }
}
