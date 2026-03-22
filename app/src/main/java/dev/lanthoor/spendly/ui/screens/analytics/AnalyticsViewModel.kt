package dev.lanthoor.spendly.ui.screens.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.lanthoor.spendly.domain.model.Category
import dev.lanthoor.spendly.domain.model.Expense
import dev.lanthoor.spendly.domain.model.Income
import dev.lanthoor.spendly.domain.model.LineChartEntry
import dev.lanthoor.spendly.domain.model.PieChartEntry
import dev.lanthoor.spendly.domain.repository.CategoryRepository
import dev.lanthoor.spendly.domain.repository.ExpenseRepository
import dev.lanthoor.spendly.domain.repository.IncomeRepository
import dev.lanthoor.spendly.domain.repository.PreferencesRepository
import dev.lanthoor.spendly.utils.ChartDataTransformer
import dev.lanthoor.spendly.utils.TimePeriod
import dev.lanthoor.spendly.utils.getDateRange
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

/**
 * ViewModel for Analytics screen.
 * Manages time period selection and provides chart data for various analytics visualizations.
 */
@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val incomeRepository: IncomeRepository,
    private val categoryRepository: CategoryRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    // Period type selection (FY / Calendar Year)
    private val _selectedPeriodType = MutableStateFlow(AnalyticsPeriodType.FINANCIAL_YEAR)
    val selectedPeriodType: StateFlow<AnalyticsPeriodType> = _selectedPeriodType

    /**
     * Select analytics period type (FY / Year).
     */
    fun selectPeriodType(periodType: AnalyticsPeriodType) {
        _selectedPeriodType.value = periodType
    }

    /**
     * Get TimePeriod based on selected period type.
     */
    private fun getTimePeriod(periodType: AnalyticsPeriodType): TimePeriod = when (periodType) {
        AnalyticsPeriodType.FINANCIAL_YEAR -> TimePeriod.ThisFinancialYear
        AnalyticsPeriodType.CALENDAR_YEAR -> TimePeriod.ThisYear
    }

    /**
     * Combined analytics state with chart data.
     */
    val analyticsState: StateFlow<AnalyticsUiState> = combine(
        expenseRepository.getAllExpenses(),
        incomeRepository.getAllIncome(),
        categoryRepository.getAllCategories(),
        _selectedPeriodType
    ) { expenses, incomes, categories, periodType ->
        try {
            val period = getTimePeriod(periodType)
            val (startDate, endDate) = period.getDateRange()

            // Filter transactions by selected time period
            val filteredExpenses = expenses.filter { expense ->
                expense.date in startDate..endDate
            }
            val filteredIncomes = incomes.filter { income ->
                income.date in startDate..endDate
            }

            // If no transactions in period, return empty state
            if (filteredExpenses.isEmpty() && filteredIncomes.isEmpty()) {
                return@combine AnalyticsUiState.Empty(period)
            }

            // Calculate current period totals
            val totalExpense = filteredExpenses.sumOf { it.amount }
            val totalIncome = filteredIncomes.sumOf { it.amount }
            val netBalance = totalIncome - totalExpense

            // Calculate previous period data for comparison
            val (prevStartDate, prevEndDate) = getPreviousPeriodRange(period)
            val prevExpenses = expenses.filter { it.date in prevStartDate..prevEndDate }
            val prevIncomes = incomes.filter { it.date in prevStartDate..prevEndDate }

            val prevExpense = prevExpenses.sumOf { it.amount }
            val prevIncome = prevIncomes.sumOf { it.amount }
            val prevNetBalance = prevIncome - prevExpense

            // Calculate percentage changes
            val expenseChange = calculatePercentageChange(prevExpense, totalExpense)
            val incomeChange = calculatePercentageChange(prevIncome, totalIncome)
            val balanceChange = calculatePercentageChange(prevNetBalance, netBalance)

            // Aggregate trend data
            val expenseTrendData = aggregateExpensesTrend(
                expenses = filteredExpenses,
                periodType = periodType,
                startDate = startDate,
                endDate = endDate
            )

            val incomeTrendData = aggregateIncomesTrend(
                incomes = filteredIncomes,
                periodType = periodType,
                startDate = startDate,
                endDate = endDate
            )

            // Calculate net worth (cumulative running total)
            val netWorthTrendData = calculateNetWorthData(
                incomeData = incomeTrendData,
                expenseData = expenseTrendData
            )

            AnalyticsUiState.Success(
                period = period,
                totalExpense = totalExpense,
                totalIncome = totalIncome,
                netBalance = netBalance,
                prevExpense = prevExpense,
                prevIncome = prevIncome,
                prevNetBalance = prevNetBalance,
                expenseChange = expenseChange,
                incomeChange = incomeChange,
                balanceChange = balanceChange,
                categoryPieChartData = emptyList(), // Will be calculated in the UI state
                filteredExpenses = filteredExpenses,
                filteredIncome = filteredIncomes,
                categories = categories,
                expenseTrendData = expenseTrendData,
                incomeTrendData = incomeTrendData,
                netWorthTrendData = netWorthTrendData
            )
        } catch (e: Exception) {
            AnalyticsUiState.Error(e.message ?: "Failed to load analytics data")
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AnalyticsUiState.Loading
    )

    /**
     * Calculate percentage change between two values.
     */
    private fun calculatePercentageChange(previous: Long, current: Long): Float {
        if (previous == 0L) {
            return if (current > 0) 100f else 0f
        }
        return ((current - previous).toFloat() / previous.toFloat()) * 100f
    }

    /**
     * Aggregate expenses by month based on period type.
     * Returns list of LineChartEntry for trend visualization.
     */
    private fun aggregateExpensesTrend(
        expenses: List<Expense>,
        periodType: AnalyticsPeriodType,
        startDate: Long,
        endDate: Long
    ): List<LineChartEntry> {
        if (expenses.isEmpty()) return emptyList()

        // Monthly aggregation for both FY and Calendar Year
        return aggregateExpensesByMonth(expenses)
    }

    /**
     * Aggregate incomes by month based on period type.
     * Returns list of LineChartEntry for trend visualization.
     */
    private fun aggregateIncomesTrend(
        incomes: List<Income>,
        periodType: AnalyticsPeriodType,
        startDate: Long,
        endDate: Long
    ): List<LineChartEntry> {
        if (incomes.isEmpty()) return emptyList()

        // Monthly aggregation for both FY and Calendar Year
        return aggregateIncomesByMonth(incomes)
    }

    /**
     * Calculate net worth as cumulative running total (Income - Expense).
     * Merges income and expense data by date and calculates running sum.
     * Supports negative values when cumulative expenses exceed cumulative income.
     */
    private fun calculateNetWorthData(
        incomeData: List<LineChartEntry>,
        expenseData: List<LineChartEntry>
    ): List<LineChartEntry> {
        // If both lists are empty, return empty
        if (incomeData.isEmpty() && expenseData.isEmpty()) return emptyList()

        // Create maps for quick lookup by date
        val incomeMap = incomeData.associateBy { it.date }
        val expenseMap = expenseData.associateBy { it.date }

        // Get all unique dates from both datasets and sort by timestamp
        val allDates = (incomeMap.keys + expenseMap.keys).toSet()
            .map { date ->
                // Get timestamp from either income or expense entry
                val timestamp = incomeMap[date]?.timestamp ?: expenseMap[date]?.timestamp ?: 0L
                date to timestamp
            }
            .sortedBy { it.second }

        // Calculate cumulative net worth
        val result = mutableListOf<LineChartEntry>()
        var cumulativeNetWorth = 0L

        allDates.forEach { (date, timestamp) ->
            val incomeAmount = incomeMap[date]?.amount ?: 0L
            val expenseAmount = expenseMap[date]?.amount ?: 0L

            // Update cumulative total
            cumulativeNetWorth += (incomeAmount - expenseAmount)

            // Get date label from either source
            val dateLabel = incomeMap[date]?.dateLabel ?: expenseMap[date]?.dateLabel ?: date

            result.add(
                LineChartEntry(
                    date = date,
                    dateLabel = dateLabel,
                    amount = cumulativeNetWorth,
                    timestamp = timestamp
                )
            )
        }

        return result
    }

    /**
     * Aggregate expenses by month.
     */
    private fun aggregateExpensesByMonth(expenses: List<Expense>): List<LineChartEntry> {
        val monthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val labelFormat = SimpleDateFormat("MMM", Locale.getDefault())
        val calendar = Calendar.getInstance()

        // Group expenses by month
        val monthlyTotals = expenses.groupBy { expense ->
            calendar.timeInMillis = expense.date
            monthFormat.format(calendar.time)
        }.mapValues { (_, expenseList) ->
            expenseList.sumOf { it.amount }
        }

        // Convert to LineChartEntry
        return monthlyTotals.map { (monthStr, amount) ->
            val parts = monthStr.split("-")
            calendar.set(Calendar.YEAR, parts[0].toInt())
            calendar.set(Calendar.MONTH, parts[1].toInt() - 1)
            calendar.set(Calendar.DAY_OF_MONTH, 1)

            LineChartEntry(
                date = monthStr,
                dateLabel = labelFormat.format(calendar.time),
                amount = amount,
                timestamp = calendar.timeInMillis
            )
        }.sortedBy { it.timestamp }
    }

    /**
     * Aggregate incomes by month.
     */
    private fun aggregateIncomesByMonth(incomes: List<Income>): List<LineChartEntry> {
        val monthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val labelFormat = SimpleDateFormat("MMM", Locale.getDefault())
        val calendar = Calendar.getInstance()

        // Group incomes by month
        val monthlyTotals = incomes.groupBy { income ->
            calendar.timeInMillis = income.date
            monthFormat.format(calendar.time)
        }.mapValues { (_, incomeList) ->
            incomeList.sumOf { it.amount }
        }

        // Convert to LineChartEntry
        return monthlyTotals.map { (monthStr, amount) ->
            val parts = monthStr.split("-")
            calendar.set(Calendar.YEAR, parts[0].toInt())
            calendar.set(Calendar.MONTH, parts[1].toInt() - 1)
            calendar.set(Calendar.DAY_OF_MONTH, 1)

            LineChartEntry(
                date = monthStr,
                dateLabel = labelFormat.format(calendar.time),
                amount = amount,
                timestamp = calendar.timeInMillis
            )
        }.sortedBy { it.timestamp }
    }

    /**
     * Get the previous period date range based on the current period type.
     * ThisMonth → LastMonth
     * LastMonth → MonthBeforeLast
     * Last3Months → Previous3Months
     * Last6Months → Previous6Months
     * ThisYear → LastYear
     * LastYear → YearBeforeLast
     * Custom → Previous period of same length
     */
    private fun getPreviousPeriodRange(period: TimePeriod): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        val (currentStart, currentEnd) = period.getDateRange()

        return when (period) {
            is TimePeriod.ThisMonth -> {
                // Go back one month
                calendar.timeInMillis = currentStart
                calendar.add(Calendar.MONTH, -1)
                val start = calendar.timeInMillis
                calendar.set(
                    Calendar.DAY_OF_MONTH,
                    calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                )
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                val end = calendar.timeInMillis
                start to end
            }

            is TimePeriod.LastMonth -> {
                // Go back to month before last
                calendar.timeInMillis = currentStart
                calendar.add(Calendar.MONTH, -1)
                val start = calendar.timeInMillis
                calendar.set(
                    Calendar.DAY_OF_MONTH,
                    calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                )
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                val end = calendar.timeInMillis
                start to end
            }

            is TimePeriod.Last3Months -> {
                // Previous 3 months (months 4-6 ago)
                calendar.timeInMillis = currentStart
                calendar.add(Calendar.MONTH, -3)
                val start = calendar.timeInMillis
                calendar.timeInMillis = currentEnd
                calendar.add(Calendar.MONTH, -3)
                val end = calendar.timeInMillis
                start to end
            }

            is TimePeriod.Last6Months -> {
                // Previous 6 months (months 7-12 ago)
                calendar.timeInMillis = currentStart
                calendar.add(Calendar.MONTH, -6)
                val start = calendar.timeInMillis
                calendar.timeInMillis = currentEnd
                calendar.add(Calendar.MONTH, -6)
                val end = calendar.timeInMillis
                start to end
            }

            is TimePeriod.ThisYear -> {
                // Last year (same calendar year range)
                calendar.timeInMillis = currentStart
                calendar.add(Calendar.YEAR, -1)
                val start = calendar.timeInMillis
                calendar.timeInMillis = currentEnd
                calendar.add(Calendar.YEAR, -1)
                val end = calendar.timeInMillis
                start to end
            }

            is TimePeriod.ThisFinancialYear -> {
                // Last financial year
                calendar.timeInMillis = currentStart
                calendar.add(Calendar.YEAR, -1)
                val start = calendar.timeInMillis
                calendar.timeInMillis = currentEnd
                calendar.add(Calendar.YEAR, -1)
                val end = calendar.timeInMillis
                start to end
            }

            is TimePeriod.LastYear -> {
                // Year before last
                calendar.timeInMillis = currentStart
                calendar.add(Calendar.YEAR, -1)
                val start = calendar.timeInMillis
                calendar.timeInMillis = currentEnd
                calendar.add(Calendar.YEAR, -1)
                val end = calendar.timeInMillis
                start to end
            }

            is TimePeriod.Custom -> {
                // Previous period of same length
                val periodLength = currentEnd - currentStart
                val start = currentStart - periodLength
                val end = currentEnd - periodLength
                start to end
            }
        }
    }

}

/**
 * UI state for Analytics screen.
 */
sealed interface AnalyticsUiState {
    /**
     * Initial loading state.
     */
    data object Loading : AnalyticsUiState

    /**
     * Empty state - no transactions in selected period.
     */
    data class Empty(val period: TimePeriod) : AnalyticsUiState

    /**
     * Success state with analytics data.
     */
    data class Success(
        val period: TimePeriod,
        val totalExpense: Long,
        val totalIncome: Long,
        val netBalance: Long,

        // Previous period comparison
        val prevExpense: Long,
        val prevIncome: Long,
        val prevNetBalance: Long,
        val expenseChange: Float,
        val incomeChange: Float,
        val balanceChange: Float,

        val categoryPieChartData: List<PieChartEntry>,
        val filteredExpenses: List<Expense>,
        val filteredIncome: List<Income>,
        val categories: List<Category>,

        // Trend data (monthly aggregation for FY/Year)
        val expenseTrendData: List<LineChartEntry>,
        val incomeTrendData: List<LineChartEntry>,
        val netWorthTrendData: List<LineChartEntry>
    ) : AnalyticsUiState {
        /**
         * Generate pie chart data with theme awareness.
         * Called from UI layer when theme is known.
         */
        fun getPieChartData(isDark: Boolean): List<PieChartEntry> {
            return ChartDataTransformer.expensesToPieChartData(
                expenses = filteredExpenses,
                categories = categories,
                isDark = isDark
            )
        }
    }

    /**
     * Error state.
     */
    data class Error(val message: String) : AnalyticsUiState
}
