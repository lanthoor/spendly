package dev.lanthoor.spendly.domain.model

/**
 * Data class for pie chart entries representing category spending breakdown.
 *
 * @property categoryId Unique identifier for the category
 * @property categoryName Display name of the category
 * @property categoryIcon Material Icon name (e.g., "restaurant")
 * @property amount Total amount spent in paise (Long to avoid precision loss)
 * @property percentage Percentage of total spending (0.0 to 100.0)
 * @property colorArgb Category color as ARGB Long token
 * @property transactionCount Number of transactions in this category
 */
data class PieChartEntry(
    val categoryId: Long,
    val categoryName: String,
    val categoryIcon: String,
    val amount: Long,
    val percentage: Float,
    val colorArgb: Long,
    val transactionCount: Int
)

/**
 * Data class for bar chart entries representing monthly income vs expense comparison.
 *
 * @property month Month identifier (e.g., "Jan 2024", "Feb 2024")
 * @property monthShort Short month label for chart display (e.g., "Jan", "Feb")
 * @property income Total income in paise for the month
 * @property expense Total expense in paise for the month
 * @property timestamp Start timestamp of the month (for sorting)
 */
data class BarChartEntry(
    val month: String,
    val monthShort: String,
    val income: Long,
    val expense: Long,
    val timestamp: Long
)

/**
 * Data class for line chart entries representing spending trends over time.
 *
 * @property date Date identifier (e.g., "2024-01-15")
 * @property dateLabel Display label for chart (e.g., "Jan 15")
 * @property amount Total amount in paise for this date
 * @property timestamp Timestamp for sorting and calculations
 */
data class LineChartEntry(
    val date: String,
    val dateLabel: String,
    val amount: Long,
    val timestamp: Long
)

/**
 * Data class for detailed category analysis with additional statistics.
 *
 * @property category Full category model
 * @property totalAmount Total amount spent in this category (paise)
 * @property percentage Percentage of total spending
 * @property transactionCount Number of transactions
 * @property averageAmount Average transaction amount (paise)
 */
data class CategoryAnalysis(
    val category: Category,
    val totalAmount: Long,
    val percentage: Float,
    val transactionCount: Int,
    val averageAmount: Long
)

/**
 * Data class for monthly totals aggregated from database.
 * Used internally before transformation to BarChartEntry.
 *
 * @property yearMonth Format: "YYYY-MM"
 * @property total Total amount in paise
 */
data class MonthlyTotal(
    val yearMonth: String,
    val total: Long
)

/**
 * Data class for daily totals aggregated from database.
 * Used internally before transformation to LineChartEntry.
 *
 * @property date Format: "YYYY-MM-DD"
 * @property total Total amount in paise
 */
data class DailyTotal(
    val date: String,
    val total: Long
)
