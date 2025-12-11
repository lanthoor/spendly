package `in`.mylullaby.spendly.domain.model

import `in`.mylullaby.spendly.utils.CurrencyUtils

/**
 * Domain model for recurring transactions.
 * Represents a template for creating automatic expense/income records.
 */
data class RecurringTransaction(
    val id: Long = 0,
    val transactionType: String, // "EXPENSE" or "INCOME"
    val amount: Long, // Amount in paise
    val categoryId: Long,
    val description: String,
    val frequency: String, // "DAILY", "WEEKLY", "MONTHLY"
    val nextDate: Long, // Unix timestamp for next occurrence
    val lastProcessed: Long?, // Unix timestamp when last transaction was created
    val paymentMethod: String? = null, // Payment method for expense (nullable for compatibility)
    val createdAt: Long,
    val modifiedAt: Long
) {
    /**
     * Converts amount from paise to rupees for display
     */
    fun fromPaise(): Double = CurrencyUtils.paiseToRupees(amount)
}
