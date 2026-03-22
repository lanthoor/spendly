package `in`.co.spendly.domain.model

import `in`.co.spendly.utils.CurrencyUtils
import `in`.co.spendly.utils.IncomeSource

/**
 * Domain model representing an income transaction.
 * All amounts are stored in paise (Long) for zero precision loss.
 */
data class Income(
    val id: Long = 0,
    val amount: Long, // in paise
    val categoryId: Long?, // income category ID (nullable for backwards compatibility)
    val source: IncomeSource, // DEPRECATED - use categoryId instead
    val date: Long, // timestamp in milliseconds
    val description: String,
    val accountId: Long, // required - defaults to Account.DEFAULT_ACCOUNT_ID (1)
    val isRecurring: Boolean = false,
    val linkedExpenseId: Long? = null, // for refunds
    val createdAt: Long,
    val modifiedAt: Long,
    val smsSourceId: Long? = null, // Link to SMS that created this income (for audit trail)
    val smsBody: String? = null, // Original SMS text for reference
    val smsConfidence: Float? = null, // Parsing confidence score (0.0-1.0)
    val smsTimestamp: Long? = null // When SMS was received (Unix timestamp)
) {
    /**
     * Converts paise amount to rupees (Double).
     * @return Amount in rupees (e.g., 10050 paise = 100.50 rupees)
     */
    fun fromPaise(): Double = amount / 100.0

    /**
     * Formats the amount as a display string in INR format.
     * @return Formatted string (e.g., "₹100.50")
     */
    fun displayAmount(): String = CurrencyUtils.formatPaise(amount)

    /**
     * Checks if this income is a refund linked to an expense.
     * @return true if linkedExpenseId is not null
     */
    fun isRefund(): Boolean = linkedExpenseId != null

    companion object {
        /**
         * Converts rupees string to paise (Long).
         * @param rupeesString Amount in rupees as string
         * @return Amount in paise
         */
        fun toPaise(rupeesString: String): Long = CurrencyUtils.parseRupeesToPaise(rupeesString)
    }
}
