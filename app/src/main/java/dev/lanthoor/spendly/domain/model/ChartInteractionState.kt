package dev.lanthoor.spendly.domain.model

import androidx.compose.ui.graphics.Color

/**
 * Data model for pie chart slice selection.
 * Represents the selected category with all relevant display information.
 *
 * @property categoryId Unique identifier for the selected category
 * @property categoryName Display name of the category
 * @property amount Total amount spent in paise
 * @property percentage Percentage of total spending (0.0 to 100.0)
 * @property transactionCount Number of transactions in this category
 * @property color Category color for visual consistency
 */
data class PieSliceSelection(
    val categoryId: Long,
    val categoryName: String,
    val amount: Long,
    val percentage: Float,
    val transactionCount: Int,
    val color: Color
)

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

/**
 * Represents an arc segment in a pie/donut chart.
 * Used internally for hit testing and rendering.
 *
 * @property startAngle Starting angle in degrees (0° = 12 o'clock, clockwise)
 * @property sweepAngle Arc length in degrees
 * @property dataIndex Index of the slice in the original data list
 */
data class PieSliceArc(
    val startAngle: Float,
    val sweepAngle: Float,
    val dataIndex: Int
)
