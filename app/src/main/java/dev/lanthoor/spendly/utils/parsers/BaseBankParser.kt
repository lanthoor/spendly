package dev.lanthoor.spendly.utils.parsers

import android.util.Log
import dev.lanthoor.spendly.utils.ParsedTransaction
import dev.lanthoor.spendly.utils.TransactionType

/**
 * Abstract base class for bank SMS parsers.
 *
 * This class implements the Template Method pattern:
 * - Generic extraction methods (amount, type, account, merchant, date) are implemented in base class
 * - Subclasses can override any method to provide bank-specific parsing logic
 * - Fallback chain: Bank-specific → Generic → Default
 *
 * All parsers extract all fields (not just basic fields), ensuring feature parity.
 */
abstract class BaseBankParser {
    companion object {
        private const val TAG = "BaseBankParser"
    }

    /**
     * Template method that orchestrates all extraction steps.
     * Final - cannot be overridden by subclasses.
     */
    fun parse(smsBody: String, smsTimestamp: Long): ParsedTransaction? {
        var confidence = 0f

        // Extract all fields - each method can be overridden by subclass
        val amount = extractAmount(smsBody)
        if (amount != null) confidence += 0.4f

        val transactionType = extractTransactionType(smsBody)
        if (transactionType != null) confidence += 0.3f

        val accountHint = extractAccountHint(smsBody)
        if (accountHint != null) confidence += 0.2f

        val merchantName = extractMerchant(smsBody)
        if (merchantName != null) confidence += 0.1f

        // Date: try to extract from SMS body first, fall back to SMS receipt timestamp
        val extractedDate = extractDate(smsBody, smsTimestamp)
        val date = if (extractedDate != null) {
            extractedDate
        } else {
            smsTimestamp
        }

        // Description: use merchant or bank-specific default
        val description = merchantName ?: getDefaultDescription(transactionType)

        // Build result if we have minimum required fields
        return buildResult(
            amount,
            transactionType,
            date,
            description,
            accountHint,
            merchantName,
            confidence
        )
    }

    /**
     * Extract amount from SMS.
     * Generic implementation - can be overridden for bank-specific formats.
     */
    protected open fun extractAmount(smsBody: String): Long? {
        val amountRegex = Regex("""(?:INR|Rs\.?|₹)\s*([\d,]+\.?\d*)""", RegexOption.IGNORE_CASE)
        return amountRegex.find(smsBody)?.groupValues?.get(1)?.let { parseAmountToPaise(it) }
    }

    /**
     * Extract transaction type from SMS.
     * Generic implementation - can be overridden for bank-specific keywords.
     */
    protected open fun extractTransactionType(smsBody: String): TransactionType? {
        return when {
            smsBody.contains("debited", ignoreCase = true) -> TransactionType.EXPENSE
            smsBody.contains("spent", ignoreCase = true) -> TransactionType.EXPENSE
            smsBody.contains("sent", ignoreCase = true) -> TransactionType.EXPENSE
            smsBody.contains("withdrawn", ignoreCase = true) -> TransactionType.EXPENSE
            smsBody.contains("credited", ignoreCase = true) -> TransactionType.INCOME
            smsBody.contains("received", ignoreCase = true) -> TransactionType.INCOME
            smsBody.contains("deposited", ignoreCase = true) -> TransactionType.INCOME
            else -> null
        }
    }

    /**
     * Extract account hint (last 4 digits) from SMS.
     * Generic implementation tries common patterns - can be overridden for bank-specific patterns.
     */
    protected open fun extractAccountHint(smsBody: String): String? {
        // Generic patterns: **1234, XX1234, Acct 1234, A/c 1234
        val patterns = listOf(
            Regex("""\*+(\d{4})"""),                                 // **1234
            Regex("""XX(\d{4})""", RegexOption.IGNORE_CASE),         // XX1234
            Regex(
                """Acct\s*\*?\s*XX?(\d{4})""",
                RegexOption.IGNORE_CASE
            ),  // Acct XX1234 or Acct 1234
            Regex("""A/c\s*\*?\s*(\d{4})""", RegexOption.IGNORE_CASE)      // A/c 1234 or A/c **1234
        )
        for (pattern in patterns) {
            pattern.find(smsBody)?.groupValues?.get(1)?.let { return it }
        }
        return null
    }

    /**
     * Extract merchant name from SMS.
     * Generic implementation tries common patterns - can be overridden for bank-specific patterns.
     */
    protected open fun extractMerchant(smsBody: String): String? {
        // Generic patterns: "at MERCHANT", "to UPI@ID", "from UPI@ID"
        val patterns = listOf(
            Regex("""at\s+([A-Z][A-Z\s]+?)(?:\s+on|\s*$)""", RegexOption.IGNORE_CASE),
            Regex("""to\s+([\w.-]+@[\w.-]+)""", RegexOption.IGNORE_CASE),   // UPI ID
            Regex("""from\s+([\w.-]+@[\w.-]+)""", RegexOption.IGNORE_CASE)  // UPI ID
        )
        for (pattern in patterns) {
            pattern.find(smsBody)?.groupValues?.get(1)?.trim()?.let { return it }
        }
        return null
    }

    /**
     * Extract date from SMS.
     * Generic implementation tries common date formats - can be overridden for bank-specific formats.
     * Returns null if no date found (caller will use SMS timestamp as fallback).
     */
    protected open fun extractDate(smsBody: String, smsTimestamp: Long): Long? {
        // Try common date formats
        val formats = listOf(
            Regex("""(\d{1,2})-([A-Za-z]{3})-(\d{2})"""),   // 13-Dec-24
            Regex("""(\d{1,2})([A-Za-z]{3})(\d{2})"""),     // 13Dec24
            Regex("""(\d{1,2})/(\d{1,2})/(\d{2,4})""")      // 13/12/24 or 13/12/2024
        )
        for (format in formats) {
            format.find(smsBody)?.let { match ->
                return parseDateFromMatch(match, smsTimestamp)
            }
        }
        return null
    }

    /**
     * Get bank-specific default description.
     * Can be overridden to provide bank-specific descriptions.
     */
    protected open fun getDefaultDescription(type: TransactionType?): String {
        return when (type) {
            TransactionType.EXPENSE -> "Bank Transaction (SMS)"
            TransactionType.INCOME -> "Bank Credit (SMS)"
            null -> "Transaction"
        }
    }

    /**
     * Helper: Parse amount string to paise.
     * Handles formats: "1,234.56", "1234.5", "1234"
     */
    protected fun parseAmountToPaise(amountStr: String): Long? {
        return try {
            val cleaned = amountStr.replace(",", "")
            val rupees = cleaned.toDoubleOrNull() ?: return null
            (rupees * 100).toLong()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse amount: $amountStr", e)
            null
        }
    }

    /**
     * Helper: Parse date from regex match.
     * Handles different date formats based on match groups.
     */
    protected fun parseDateFromMatch(match: MatchResult, smsTimestamp: Long): Long? {
        return try {
            val groups = match.groupValues
            if (groups.size < 4) return null

            val day = groups[1].toIntOrNull() ?: return null
            val smsTimeCalendar = java.util.Calendar.getInstance().apply {
                timeInMillis = smsTimestamp
            }
            val smsYear = smsTimeCalendar.get(java.util.Calendar.YEAR)

            // Detect format and parse month/year accordingly
            val (month, year) = if (groups[2].length == 3) {
                // Format: 13-Dec-24 or 13Dec24 (month is text)
                val monthMap = mapOf(
                    "jan" to 0, "feb" to 1, "mar" to 2, "apr" to 3,
                    "may" to 4, "jun" to 5, "jul" to 6, "aug" to 7,
                    "sep" to 8, "oct" to 9, "nov" to 10, "dec" to 11
                )
                val month = monthMap[groups[2].lowercase()] ?: return null
                val year = when (groups[3].length) {
                    2 -> (smsYear / 100) * 100 + groups[3].toInt()  // 24 -> 2024
                    4 -> groups[3].toInt()  // 2024 -> 2024
                    else -> return null
                }
                Pair(month, year)
            } else {
                // Format: 13/12/24 or 13/12/2024 (month is numeric)
                val month = (groups[2].toIntOrNull() ?: return null) - 1  // 1-12 -> 0-11
                val year = when (groups[3].length) {
                    2 -> (smsYear / 100) * 100 + groups[3].toInt()  // 24 -> 2024
                    4 -> groups[3].toInt()  // 2024 -> 2024
                    else -> return null
                }
                Pair(month, year)
            }

            // Create Calendar object and set date components.
            // Keep time from SMS receipt timestamp to avoid midnight-only dates.
            java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.YEAR, year)
                set(java.util.Calendar.MONTH, month)
                set(java.util.Calendar.DAY_OF_MONTH, day)
                set(java.util.Calendar.HOUR_OF_DAY, smsTimeCalendar.get(java.util.Calendar.HOUR_OF_DAY))
                set(java.util.Calendar.MINUTE, smsTimeCalendar.get(java.util.Calendar.MINUTE))
                set(java.util.Calendar.SECOND, smsTimeCalendar.get(java.util.Calendar.SECOND))
                set(java.util.Calendar.MILLISECOND, smsTimeCalendar.get(java.util.Calendar.MILLISECOND))
            }.timeInMillis
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse date: ${match.value}", e)
            null
        }
    }

    private fun buildResult(
        amount: Long?,
        transactionType: TransactionType?,
        date: Long,
        description: String,
        accountHint: String?,
        merchantName: String?,
        confidence: Float
    ): ParsedTransaction? {
        return if (amount != null && transactionType != null && confidence >= 0.7f) {
            ParsedTransaction(
                amount = amount,
                transactionType = transactionType,
                date = date,
                description = description,
                accountHint = accountHint,
                merchantName = merchantName,
                confidence = confidence
            )
        } else {
            null
        }
    }
}
