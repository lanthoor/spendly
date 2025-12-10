package `in`.mylullaby.spendly.domain.model

import `in`.mylullaby.spendly.utils.CurrencyUtils

/**
 * Domain model representing a monthly budget.
 * Supports both category-specific and overall budgets.
 * All amounts are stored in paise (Long) for zero precision loss.
 */
data class Budget(
    val id: Long = 0,
    val categoryId: Long? = null, // null = overall budget
    val amount: Long, // in paise
    val month: Int, // 1-12
    val year: Int, // e.g., 2025
    val notification75Sent: Boolean = false,
    val notification100Sent: Boolean = false
) {
    /**
     * Formats the amount as a display string in INR format.
     * @return Formatted string (e.g., "₹5,000.00")
     */
    fun displayAmount(): String = CurrencyUtils.formatPaise(amount)

    /**
     * Checks if this is an overall budget (not category-specific).
     * @return true if categoryId is null
     */
    fun isOverallBudget(): Boolean = categoryId == null

    /**
     * Calculates the budget progress percentage.
     * @param spent Amount spent in paise
     * @return Progress percentage (0.0 to 100.0+)
     */
    fun calculateProgress(spent: Long): Float {
        return if (amount > 0) {
            (spent.toFloat() / amount) * 100f
        } else {
            0f
        }
    }

    /**
     * Checks if the 75% notification should be sent.
     * @param spent Amount spent in paise
     * @return true if notification hasn't been sent and progress >= 75%
     */
    fun shouldNotify75(spent: Long): Boolean =
        !notification75Sent && calculateProgress(spent) >= 75f

    /**
     * Checks if the 100% notification should be sent.
     * @param spent Amount spent in paise
     * @return true if notification hasn't been sent and progress >= 100%
     */
    fun shouldNotify100(spent: Long): Boolean =
        !notification100Sent && calculateProgress(spent) >= 100f

    companion object {
        /**
         * Budget alert threshold for 75% notification.
         */
        const val ALERT_THRESHOLD_75 = 75f

        /**
         * Budget alert threshold for 100% notification.
         */
        const val ALERT_THRESHOLD_100 = 100f
    }
}
