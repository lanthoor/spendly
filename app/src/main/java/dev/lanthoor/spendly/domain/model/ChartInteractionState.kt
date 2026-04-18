package dev.lanthoor.spendly.domain.model

/**
 * Data model for line chart point selection.
 * Represents daily or monthly aggregated data at a specific point.
 *
 * @property date Date identifier (e.g., "2024-01-15" for daily, "Jan 2024" for monthly)
 * @property dateLabel Display label for UI (e.g., "Jan 15", "January")
 * @property incomeAmount Total income in paise for this date (null if no income)
 * @property expenseAmount Total expense in paise for this date (null if no expense)
 * @property timestamp Unix timestamp for sorting and calculations
 */
data class LinePointSelection(
    val date: String,
    val dateLabel: String,
    val incomeAmount: Long?,
    val expenseAmount: Long?,
    val timestamp: Long
)

/**
 * Represents a calculated point in chart coordinate space.
 * Used internally for rendering and hit testing.
 *
 * @property x X-coordinate in pixels (Canvas space)
 * @property y Y-coordinate in pixels (Canvas space)
 * @property dataIndex Index of the data point in the original data list
 */
data class ChartPoint(
    val x: Float,
    val y: Float,
    val dataIndex: Int
)
