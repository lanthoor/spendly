package dev.lanthoor.spendly.domain.usecase.dashboard

import dev.lanthoor.spendly.core.model.finance.BudgetWithProgress
import dev.lanthoor.spendly.core.model.finance.RecentTransaction
import dev.lanthoor.spendly.core.model.preferences.YearType
import dev.lanthoor.spendly.domain.model.Budget
import dev.lanthoor.spendly.domain.model.Category
import dev.lanthoor.spendly.domain.model.Expense
import dev.lanthoor.spendly.domain.model.Income
import java.util.Calendar

data class DashboardSummaryInput(
    val expenses: List<Expense>,
    val incomes: List<Income>,
    val categories: List<Category>,
    val allBudgets: List<Budget>,
    val selectedMonth: Int,
    val selectedYear: Int,
    val yearType: YearType
)

data class DashboardFinancialSummary(
    val selectedMonth: Int,
    val selectedYear: Int,
    val monthExpenses: Long,
    val monthIncome: Long,
    val monthNetBalance: Long,
    val monthExpenseChange: Float,
    val monthIncomeChange: Float,
    val monthBalanceChange: Float,
    val ytdExpenses: Long,
    val ytdIncome: Long,
    val ytdNetBalance: Long,
    val ytdExpenseChange: Float,
    val ytdIncomeChange: Float,
    val ytdBalanceChange: Float,
    val yearType: YearType
)

data class DashboardCategorySpending(
    val category: Category,
    val totalAmount: Long,
    val transactionCount: Int
)

data class DashboardSummaryResult(
    val financialSummary: DashboardFinancialSummary,
    val recentTransactions: List<RecentTransaction>,
    val topCategories: List<DashboardCategorySpending>,
    val budgets: List<BudgetWithProgress>,
    val hasTransactions: Boolean
)

class BuildDashboardSummaryUseCase {
    fun execute(input: DashboardSummaryInput): DashboardSummaryResult {
        val monthStart = DashboardDateUtils.getMonthStartMillis(input.selectedYear, input.selectedMonth)
        val monthEnd = DashboardDateUtils.getMonthEndMillis(input.selectedYear, input.selectedMonth)

        val monthExpenses = input.expenses.filter { it.date in monthStart..monthEnd }
        val monthIncome = input.incomes.filter { it.date in monthStart..monthEnd }
        val monthExpenseTotal = monthExpenses.sumOf { it.amount }
        val monthIncomeTotal = monthIncome.sumOf { it.amount }
        val monthNetBalance = monthIncomeTotal - monthExpenseTotal

        val prevCalendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, input.selectedYear)
            set(Calendar.MONTH, input.selectedMonth - 1)
            add(Calendar.MONTH, -1)
        }
        val prevYear = prevCalendar.get(Calendar.YEAR)
        val prevMonth = prevCalendar.get(Calendar.MONTH) + 1
        val prevMonthStart = DashboardDateUtils.getMonthStartMillis(prevYear, prevMonth)
        val prevMonthEnd = DashboardDateUtils.getMonthEndMillis(prevYear, prevMonth)
        val prevMonthExpenses = input.expenses.filter { it.date in prevMonthStart..prevMonthEnd }
        val prevMonthIncome = input.incomes.filter { it.date in prevMonthStart..prevMonthEnd }
        val prevMonthExpenseTotal = prevMonthExpenses.sumOf { it.amount }
        val prevMonthIncomeTotal = prevMonthIncome.sumOf { it.amount }
        val prevMonthNetBalance = prevMonthIncomeTotal - prevMonthExpenseTotal

        val monthExpenseChange =
            DashboardCalculators.calculatePercentageChange(prevMonthExpenseTotal, monthExpenseTotal)
        val monthIncomeChange =
            DashboardCalculators.calculatePercentageChange(prevMonthIncomeTotal, monthIncomeTotal)
        val monthBalanceChange =
            DashboardCalculators.calculatePercentageChange(prevMonthNetBalance, monthNetBalance)

        val (ytdStartYear, ytdStartMonth) =
            input.yearType.getYearStart(input.selectedYear, input.selectedMonth)
        val ytdStart = DashboardDateUtils.getMonthStartMillis(ytdStartYear, ytdStartMonth)
        val (ytdEndYear, ytdEndMonth) = when (input.yearType) {
            YearType.CALENDAR -> input.selectedYear to 12
            YearType.FINANCIAL -> if (input.selectedMonth >= 4) {
                input.selectedYear + 1 to 3
            } else {
                input.selectedYear to 3
            }
        }
        val ytdEnd = DashboardDateUtils.getMonthEndMillis(ytdEndYear, ytdEndMonth)

        val ytdExpenses = input.expenses.filter { it.date in ytdStart..ytdEnd }
        val ytdIncome = input.incomes.filter { it.date in ytdStart..ytdEnd }
        val ytdExpenseTotal = ytdExpenses.sumOf { it.amount }
        val ytdIncomeTotal = ytdIncome.sumOf { it.amount }
        val ytdNetBalance = ytdIncomeTotal - ytdExpenseTotal

        val prevYtdStart = DashboardDateUtils.getMonthStartMillis(ytdStartYear - 1, ytdStartMonth)
        val prevYtdEnd = DashboardDateUtils.getMonthEndMillis(ytdEndYear - 1, ytdEndMonth)
        val prevYtdExpenses = input.expenses.filter { it.date in prevYtdStart..prevYtdEnd }
        val prevYtdIncome = input.incomes.filter { it.date in prevYtdStart..prevYtdEnd }
        val prevYtdExpenseTotal = prevYtdExpenses.sumOf { it.amount }
        val prevYtdIncomeTotal = prevYtdIncome.sumOf { it.amount }
        val prevYtdNetBalance = prevYtdIncomeTotal - prevYtdExpenseTotal

        val ytdExpenseChange =
            DashboardCalculators.calculatePercentageChange(prevYtdExpenseTotal, ytdExpenseTotal)
        val ytdIncomeChange =
            DashboardCalculators.calculatePercentageChange(prevYtdIncomeTotal, ytdIncomeTotal)
        val ytdBalanceChange =
            DashboardCalculators.calculatePercentageChange(prevYtdNetBalance, ytdNetBalance)

        return DashboardSummaryResult(
            financialSummary = DashboardFinancialSummary(
                selectedMonth = input.selectedMonth,
                selectedYear = input.selectedYear,
                monthExpenses = monthExpenseTotal,
                monthIncome = monthIncomeTotal,
                monthNetBalance = monthNetBalance,
                monthExpenseChange = monthExpenseChange,
                monthIncomeChange = monthIncomeChange,
                monthBalanceChange = monthBalanceChange,
                ytdExpenses = ytdExpenseTotal,
                ytdIncome = ytdIncomeTotal,
                ytdNetBalance = ytdNetBalance,
                ytdExpenseChange = ytdExpenseChange,
                ytdIncomeChange = ytdIncomeChange,
                ytdBalanceChange = ytdBalanceChange,
                yearType = input.yearType
            ),
            recentTransactions = DashboardCalculators.buildRecentTransactions(
                monthExpenses,
                monthIncome
            ).take(5),
            topCategories = DashboardCalculators.getTopCategories(monthExpenses, input.categories),
            budgets = DashboardCalculators.toBudgetsWithProgress(
                allBudgets = input.allBudgets,
                selectedMonth = input.selectedMonth,
                selectedYear = input.selectedYear,
                categories = input.categories,
                monthExpenses = monthExpenses
            ),
            hasTransactions = input.expenses.isNotEmpty() || input.incomes.isNotEmpty()
        )
    }
}
