package dev.lanthoor.spendly.utils

import dev.lanthoor.spendly.domain.model.Category
import java.util.Locale

import dev.lanthoor.spendly.core.model.finance.TransactionType

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
    private const val INVESTMENTS_CATEGORY_NAME = "Investments"
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

    private val investmentStrictVendorPatterns = buildPatterns(
        listOf(
            "cams",
            "cams online",
            "camsonline",
            "mycams",
            "kfintech",
            "kfin",
            "karvy",
            "mf utility",
            "mfu",
            "mfcentral",
            "bse star mf",
            "bsestarmf",
            "nse nmfii",
            "nse mf",
            "amfi",
            "kuvera",
            "etmoney",
            "fundsindia",
            "scripbox",
            "nj e wealth",
            "fisdom",
            "sbi mutual fund",
            "sbimf",
            "hdfc mutual fund",
            "hdfcmf",
            "icici prudential mutual fund",
            "icicipru",
            "iciciprumf",
            "nippon india mutual fund",
            "nipponmf",
            "axis mutual fund",
            "axismf",
            "kotak mutual fund",
            "kotakmf",
            "uti mutual fund",
            "utimf",
            "aditya birla sun life mutual fund",
            "adityabirla",
            "absl",
            "abslmf",
            "tata mutual fund",
            "dsp mutual fund",
            "mirae asset mutual fund",
            "motilal oswal mutual fund",
            "motilal oswal",
            "pgim india mutual fund",
            "franklin templeton",
            "canara robeco",
            "invesco mutual fund",
            "hsbc mutual fund",
            "mahindra manulife",
            "sundaram mutual fund",
            "bandhan mutual fund",
            "idfc mutual fund",
            "lic mutual fund",
            "iti mutual fund",
            "baroda bnp paribas mutual fund",
            "union mutual fund",
            "quant mutual fund",
            "jm financial mutual fund",
            "edelweiss mutual fund",
            "whiteoak capital mutual fund",
            "trust mutual fund"
        )
    )

    private val investmentMixedPlatformPatterns = buildPatterns(
        listOf(
            "groww",
            "indmoney",
            "paytm money",
            "upstox"
        )
    )

    private val investmentContextPatterns = buildPatterns(
        listOf(
            "mutual fund",
            "sip",
            "systematic investment plan",
            "folio",
            "amc",
            "units",
            "nav",
            "lumpsum",
            "stp",
            "swp",
            "purchase",
            "redemption"
        )
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

        if (parsed.transactionType == TransactionType.EXPENSE) {
            matchInvestmentHeuristic(merchantBlob, descriptionBlob, bodyBlob)?.let { return it }
        }

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

    private fun matchInvestmentHeuristic(
        merchantBlob: String,
        descriptionBlob: String,
        bodyBlob: String
    ): SmsCategoryMatch? {
        var score = 0f
        var merchantHits = 0
        var strictVendorHits = 0
        var mixedPlatformHits = 0
        var contextHits = 0
        val hits = mutableListOf<String>()

        investmentStrictVendorPatterns.forEach { pattern ->
            if (containsKeyword(merchantBlob, pattern)) {
                score += 6f
                merchantHits++
                strictVendorHits++
                hits += "merchant:${pattern.keyword}"
            } else if (containsKeyword(descriptionBlob, pattern)) {
                score += 4f
                strictVendorHits++
                hits += "description:${pattern.keyword}"
            } else if (containsKeyword(bodyBlob, pattern)) {
                score += 3f
                strictVendorHits++
                hits += "body:${pattern.keyword}"
            }
        }

        investmentMixedPlatformPatterns.forEach { pattern ->
            if (containsKeyword(merchantBlob, pattern)) {
                score += 3f
                merchantHits++
                mixedPlatformHits++
                hits += "merchant:${pattern.keyword}"
            } else if (containsKeyword(descriptionBlob, pattern)) {
                score += 2f
                mixedPlatformHits++
                hits += "description:${pattern.keyword}"
            } else if (containsKeyword(bodyBlob, pattern)) {
                score += 1.5f
                mixedPlatformHits++
                hits += "body:${pattern.keyword}"
            }
        }

        investmentContextPatterns.forEach { pattern ->
            if (containsKeyword(merchantBlob, pattern)) {
                score += 3f
                merchantHits++
                contextHits++
                hits += "merchant:${pattern.keyword}"
            } else if (containsKeyword(descriptionBlob, pattern)) {
                score += 2f
                contextHits++
                hits += "description:${pattern.keyword}"
            } else if (containsKeyword(bodyBlob, pattern)) {
                score += 1.5f
                contextHits++
                hits += "body:${pattern.keyword}"
            }
        }

        val hasStrictVendorSignal = strictVendorHits > 0
        val hasMixedPlatformSignal = mixedPlatformHits > 0
        val hasMutualFundPhrase = hits.any {
            it.endsWith(":mutual fund") || it.endsWith(":systematic investment plan")
        }

        val shouldClassify = when {
            hasStrictVendorSignal -> score >= 3f
            hasMixedPlatformSignal -> contextHits > 0 && score >= 4f
            else -> hasMutualFundPhrase && contextHits >= 2 && score >= 3f
        }

        if (!shouldClassify) return null

        return SmsCategoryMatch(
            categoryName = INVESTMENTS_CATEGORY_NAME,
            score = score,
            reason = hits.take(2).joinToString(", ")
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

    private fun buildPatterns(keywords: List<String>): List<KeywordPattern> {
        return keywords.map { keyword ->
            KeywordPattern(keyword = keyword, regex = buildKeywordRegex(keyword))
        }
    }

    private fun buildKeywordRegex(rawKeyword: String): Regex {
        val keyword = normalizeText(rawKeyword)
        if (keyword.isBlank()) return Regex("(?!x)x")
        val pattern = if (keyword.contains(' ')) {
            "\\b" + keyword
                .split(" ")
                .joinToString("\\s+") { Regex.escape(it) } + "\\b"
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
