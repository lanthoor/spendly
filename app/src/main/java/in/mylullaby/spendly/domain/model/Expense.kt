package `in`.mylullaby.spendly.domain.model

import `in`.mylullaby.spendly.utils.CurrencyUtils
import `in`.mylullaby.spendly.utils.PaymentMethod

/**
 * Domain model representing an expense transaction.
 * All amounts are stored in paise (Long) for zero precision loss.
 */
data class Expense(
    val id: Long = 0,
    val amount: Long, // in paise
    val categoryId: Long?, // nullable - defaults to Uncategorized
    val date: Long, // timestamp in milliseconds
    val description: String,
    val paymentMethod: PaymentMethod,
    val createdAt: Long,
    val modifiedAt: Long,
    val receipts: List<Receipt> = emptyList()
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

    companion object {
        /**
         * Converts rupees string to paise (Long).
         * @param rupeesString Amount in rupees as string
         * @return Amount in paise
         */
        fun toPaise(rupeesString: String): Long = CurrencyUtils.parseRupeesToPaise(rupeesString)
    }
}
