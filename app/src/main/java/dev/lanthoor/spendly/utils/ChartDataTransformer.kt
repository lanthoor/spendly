package dev.lanthoor.spendly.utils

import androidx.compose.ui.graphics.Color
import dev.lanthoor.spendly.data.local.dao.CategoryExpenseSummary
import dev.lanthoor.spendly.data.local.dao.DailyExpenseSummary
import dev.lanthoor.spendly.data.local.dao.DailyIncomeSummary
import dev.lanthoor.spendly.data.local.dao.MonthlyExpenseSummary
import dev.lanthoor.spendly.data.local.dao.MonthlyIncomeSummary
import dev.lanthoor.spendly.domain.model.BarChartEntry
import dev.lanthoor.spendly.domain.model.Category
import dev.lanthoor.spendly.domain.model.CategoryAnalysis
import dev.lanthoor.spendly.domain.model.Expense
import dev.lanthoor.spendly.domain.model.LineChartEntry
import dev.lanthoor.spendly.domain.model.PieChartEntry
import dev.lanthoor.spendly.ui.theme.adjustForTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Utility object for transforming domain data into chart-friendly formats.
 * Handles data aggregation, percentage calculations, and date formatting for various chart types.
 */
object ChartDataTransformer {

    /**
     * Transform expenses grouped by category into pie chart data.
     * Calculates percentages and applies theme-aware colors.
     *
     * @param expenses List of expenses to aggregate
     * @param categories All available categories for lookup
     * @param isDark Whether dark theme is active (for color adjustment)
     * @return List of pie chart entries sorted by amount (descending)
     */
    fun expensesToPieChartData(
        expenses: List<Expense>,
        categories: List<Category>,
        isDark: Boolean
    ): List<PieChartEntry> {
        if (expenses.isEmpty()) return emptyList()

        // Group expenses by category and calculate totals
        val categoryMap = categories.associateBy { it.id }
        val grouped = expenses
            .filter { it.categoryId != null } // Filter out expenses without category
            .groupBy { it.categoryId!! } // Safe to use !! after filter
        val totalAmount = expenses.sumOf { it.amount }

        return grouped.mapNotNull { (categoryId, expenseList) ->
            val category = categoryMap[categoryId] ?: return@mapNotNull null
            val amount = expenseList.sumOf { it.amount }
            val percentage = (amount.toFloat() / totalAmount.toFloat()) * 100f

            PieChartEntry(
                categoryId = categoryId,
                categoryName = category.name,
                categoryIcon = category.icon,
                amount = amount,
                percentage = percentage,
                color = Color(category.color).adjustForTheme(isDark),
                transactionCount = expenseList.size
            )
        }.sortedByDescending { it.amount }
    }

    /**
     * Transform category expense summaries (from DAO) into pie chart data.
     * Used when data is already aggregated at the database level.
     *
     * @param summaries Pre-aggregated category summaries from DAO
     * @param categories All available categories for lookup
     * @param transactionCounts Map of category ID to transaction count
     * @param isDark Whether dark theme is active
     * @return List of pie chart entries
     */
    fun categoryExpenseSummariesToPieChartData(
        summaries: List<CategoryExpenseSummary>,
        categories: List<Category>,
        transactionCounts: Map<Long, Int>,
        isDark: Boolean
    ): List<PieChartEntry> {
        if (summaries.isEmpty()) return emptyList()

        val categoryMap = categories.associateBy { it.id }
        val totalAmount = summaries.sumOf { it.total }

        return summaries.mapNotNull { summary ->
            val categoryId = summary.categoryId ?: return@mapNotNull null
            val category = categoryMap[categoryId] ?: return@mapNotNull null
            val percentage = (summary.total.toFloat() / totalAmount.toFloat()) * 100f

            PieChartEntry(
                categoryId = categoryId,
                categoryName = category.name,
                categoryIcon = category.icon,
                amount = summary.total,
                percentage = percentage,
                color = Color(category.color).adjustForTheme(isDark),
                transactionCount = transactionCounts[categoryId] ?: 0
            )
        }.sortedByDescending { it.amount }
    }

    /**
     * Transform monthly expense and income summaries into bar chart data.
     * Combines income and expense data by month for comparison.
     *
     * @param expenseSummaries Monthly expense totals from DAO
     * @param incomeSummaries Monthly income totals from DAO
     * @return List of bar chart entries sorted by date
     */
    fun monthlyTotalsToBarChartData(
        expenseSummaries: List<MonthlyExpenseSummary>,
        incomeSummaries: List<MonthlyIncomeSummary>
    ): List<BarChartEntry> {
        // Create maps for quick lookup
        val expenseMap = expenseSummaries.associateBy { it.month }
        val incomeMap = incomeSummaries.associateBy { it.month }

        // Get all unique months from both sources
        val allMonths = (expenseMap.keys + incomeMap.keys).toSet().sorted()

        return allMonths.map { month ->
            val expense = expenseMap[month]?.total ?: 0L
            val income = incomeMap[month]?.total ?: 0L

            // Parse month for display labels
            val calendar = Calendar.getInstance()
            val parts = month.split("-")
            calendar.set(Calendar.YEAR, parts[0].toInt())
            calendar.set(Calendar.MONTH, parts[1].toInt() - 1) // Month is 0-indexed

            val monthShort = SimpleDateFormat("MMM", Locale.getDefault()).format(calendar.time)
            val monthFull = SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(calendar.time)

            BarChartEntry(
                month = monthFull,
                monthShort = monthShort,
                income = income,
                expense = expense,
                timestamp = calendar.timeInMillis
            )
        }
    }

    /**
     * Transform daily expense summaries into line chart data for trend visualization.
     *
     * @param dailySummaries Daily expense totals from DAO
     * @return List of line chart entries sorted by date
     */
    fun dailyExpensesToLineChartData(
        dailySummaries: List<DailyExpenseSummary>
    ): List<LineChartEntry> {
        return dailySummaries.map { summary ->
            val calendar = Calendar.getInstance()
            val parts = summary.day.split("-")
            calendar.set(Calendar.YEAR, parts[0].toInt())
            calendar.set(Calendar.MONTH, parts[1].toInt() - 1)
            calendar.set(Calendar.DAY_OF_MONTH, parts[2].toInt())

            val dateLabel = SimpleDateFormat("MMM dd", Locale.getDefault()).format(calendar.time)

            LineChartEntry(
                date = summary.day,
                dateLabel = dateLabel,
                amount = summary.total,
                timestamp = calendar.timeInMillis
            )
        }
    }

    /**
     * Transform daily income summaries into line chart data.
     *
     * @param dailySummaries Daily income totals from DAO
     * @return List of line chart entries sorted by date
     */
    fun dailyIncomeToLineChartData(
        dailySummaries: List<DailyIncomeSummary>
    ): List<LineChartEntry> {
        return dailySummaries.map { summary ->
            val calendar = Calendar.getInstance()
            val parts = summary.day.split("-")
            calendar.set(Calendar.YEAR, parts[0].toInt())
            calendar.set(Calendar.MONTH, parts[1].toInt() - 1)
            calendar.set(Calendar.DAY_OF_MONTH, parts[2].toInt())

            val dateLabel = SimpleDateFormat("MMM dd", Locale.getDefault()).format(calendar.time)

            LineChartEntry(
                date = summary.day,
                dateLabel = dateLabel,
                amount = summary.total,
                timestamp = calendar.timeInMillis
            )
        }
    }

    /**
     * Transform expenses grouped by category into detailed category analysis.
     * Includes additional statistics like average transaction amount.
     *
     * @param expenses List of expenses to analyze
     * @param categories All available categories
     * @return List of category analysis sorted by total amount (descending)
     */
    fun expensesToCategoryAnalysis(
        expenses: List<Expense>,
        categories: List<Category>
    ): List<CategoryAnalysis> {
        if (expenses.isEmpty()) return emptyList()

        val categoryMap = categories.associateBy { it.id }
        val grouped = expenses
            .filter { it.categoryId != null } // Filter out expenses without category
            .groupBy { it.categoryId!! } // Safe to use !! after filter
        val totalAmount = expenses.sumOf { it.amount }

        return grouped.mapNotNull { (categoryId, expenseList) ->
            val category = categoryMap[categoryId] ?: return@mapNotNull null
            val amount = expenseList.sumOf { it.amount }
            val percentage = (amount.toFloat() / totalAmount.toFloat()) * 100f
            val averageAmount = amount / expenseList.size

            CategoryAnalysis(
                category = category,
                totalAmount = amount,
                percentage = percentage,
                transactionCount = expenseList.size,
                averageAmount = averageAmount
            )
        }.sortedByDescending { it.totalAmount }
    }

    /**
     * Fill gaps in daily data with zero values for continuous line charts.
     * Ensures every day in the range has a data point, even if no transactions occurred.
     *
     * @param entries Existing line chart entries
     * @param startDate Start of date range
     * @param endDate End of date range
     * @return List with filled gaps, sorted by date
     */
    fun fillDailyGaps(
        entries: List<LineChartEntry>,
        startDate: Long,
        endDate: Long
    ): List<LineChartEntry> {
        if (entries.isEmpty()) return emptyList()

        val entryMap = entries.associateBy { it.date }
        val result = mutableListOf<LineChartEntry>()

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = startDate
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val labelFormat = SimpleDateFormat("MMM dd", Locale.getDefault())

        while (calendar.timeInMillis <= endDate) {
            val dateStr = dateFormat.format(calendar.time)
            val existing = entryMap[dateStr]

            if (existing != null) {
                result.add(existing)
            } else {
                // Add zero entry for missing date
                result.add(
                    LineChartEntry(
                        date = dateStr,
                        dateLabel = labelFormat.format(calendar.time),
                        amount = 0L,
                        timestamp = calendar.timeInMillis
                    )
                )
            }

            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        return result
    }
}
