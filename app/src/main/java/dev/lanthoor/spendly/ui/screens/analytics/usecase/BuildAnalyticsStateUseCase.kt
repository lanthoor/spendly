package dev.lanthoor.spendly.ui.screens.analytics.usecase

import dev.lanthoor.spendly.core.model.preferences.TimePeriod
import dev.lanthoor.spendly.core.model.preferences.getDateRange
import dev.lanthoor.spendly.domain.model.Category
import dev.lanthoor.spendly.domain.model.Expense
import dev.lanthoor.spendly.domain.model.Income
import dev.lanthoor.spendly.domain.model.LineChartEntry
import dev.lanthoor.spendly.ui.screens.analytics.AnalyticsPeriodRangeCalculator
import dev.lanthoor.spendly.ui.screens.analytics.AnalyticsPeriodType
import dev.lanthoor.spendly.ui.screens.analytics.AnalyticsTrendCalculator
import javax.inject.Inject

data class AnalyticsStateInput(
    val expenses: List<Expense>,
    val incomes: List<Income>,
    val categories: List<Category>,
    val periodType: AnalyticsPeriodType
)

data class AnalyticsStateResult(
    val period: TimePeriod,
    val totalExpense: Long,
    val totalIncome: Long,
    val netBalance: Long,
    val prevExpense: Long,
    val prevIncome: Long,
    val prevNetBalance: Long,
    val expenseChange: Float,
    val incomeChange: Float,
    val balanceChange: Float,
    val filteredExpenses: List<Expense>,
    val filteredIncome: List<Income>,
    val categories: List<Category>,
    val expenseTrendData: List<LineChartEntry>,
    val incomeTrendData: List<LineChartEntry>,
    val netWorthTrendData: List<LineChartEntry>
)

class BuildAnalyticsStateUseCase @Inject constructor() {
    fun execute(input: AnalyticsStateInput): AnalyticsStateResult {
        val period = getTimePeriod(input.periodType)
        val (startDate, endDate) = period.getDateRange()

        val filteredExpenses = input.expenses.filter { it.date in startDate..endDate }
        val filteredIncomes = input.incomes.filter { it.date in startDate..endDate }

        val totalExpense = filteredExpenses.sumOf { it.amount }
        val totalIncome = filteredIncomes.sumOf { it.amount }
        val netBalance = totalIncome - totalExpense

        val (prevStartDate, prevEndDate) = AnalyticsPeriodRangeCalculator.getPreviousPeriodRange(period)
        val prevExpenses = input.expenses.filter { it.date in prevStartDate..prevEndDate }
        val prevIncomes = input.incomes.filter { it.date in prevStartDate..prevEndDate }
        val prevExpense = prevExpenses.sumOf { it.amount }
        val prevIncome = prevIncomes.sumOf { it.amount }
        val prevNetBalance = prevIncome - prevExpense

        val expenseTrendData = aggregateExpensesTrend(filteredExpenses)
        val incomeTrendData = aggregateIncomesTrend(filteredIncomes)
        val netWorthTrendData = AnalyticsTrendCalculator.calculateNetWorthData(
            incomeData = incomeTrendData,
            expenseData = expenseTrendData
        )

        return AnalyticsStateResult(
            period = period,
            totalExpense = totalExpense,
            totalIncome = totalIncome,
            netBalance = netBalance,
            prevExpense = prevExpense,
            prevIncome = prevIncome,
            prevNetBalance = prevNetBalance,
            expenseChange = calculatePercentageChange(prevExpense, totalExpense),
            incomeChange = calculatePercentageChange(prevIncome, totalIncome),
            balanceChange = calculatePercentageChange(prevNetBalance, netBalance),
            filteredExpenses = filteredExpenses,
            filteredIncome = filteredIncomes,
            categories = input.categories,
            expenseTrendData = expenseTrendData,
            incomeTrendData = incomeTrendData,
            netWorthTrendData = netWorthTrendData
        )
    }

    private fun getTimePeriod(periodType: AnalyticsPeriodType): TimePeriod = when (periodType) {
        AnalyticsPeriodType.FINANCIAL_YEAR -> TimePeriod.ThisFinancialYear
        AnalyticsPeriodType.CALENDAR_YEAR -> TimePeriod.ThisYear
    }

    private fun calculatePercentageChange(previous: Long, current: Long): Float {
        if (previous == 0L) {
            return if (current > 0) 100f else 0f
        }
        return ((current - previous).toFloat() / previous.toFloat()) * 100f
    }

    private fun aggregateExpensesTrend(expenses: List<Expense>): List<LineChartEntry> {
        if (expenses.isEmpty()) return emptyList()
        return AnalyticsTrendCalculator.aggregateExpensesByMonth(expenses)
    }

    private fun aggregateIncomesTrend(incomes: List<Income>): List<LineChartEntry> {
        if (incomes.isEmpty()) return emptyList()
        return AnalyticsTrendCalculator.aggregateIncomesByMonth(incomes)
    }
}
