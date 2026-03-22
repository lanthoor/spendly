package `in`.co.spendly.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.co.spendly.domain.model.Account
import `in`.co.spendly.domain.model.Budget
import `in`.co.spendly.domain.model.Category
import `in`.co.spendly.domain.model.Expense
import `in`.co.spendly.domain.model.Income
import `in`.co.spendly.domain.repository.AccountRepository
import `in`.co.spendly.domain.repository.BudgetRepository
import `in`.co.spendly.domain.repository.CategoryRepository
import `in`.co.spendly.domain.repository.ExpenseRepository
import `in`.co.spendly.domain.repository.IncomeRepository
import `in`.co.spendly.domain.repository.PreferencesRepository
import `in`.co.spendly.ui.screens.budgets.BudgetWithProgress
import `in`.co.spendly.utils.YearType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

/**
 * ViewModel for managing dashboard screen.
 * Provides combined financial overview of expenses and income.
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val incomeRepository: IncomeRepository,
    private val categoryRepository: CategoryRepository,
    private val accountRepository: AccountRepository,
    private val budgetRepository: BudgetRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    // Selected month state (default to current month)
    private val _selectedMonth = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH) + 1)
    private val _selectedYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))

    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    /**
     * Select a specific month and year for dashboard display.
     */
    fun selectMonth(year: Int, month: Int) {
        _selectedYear.value = year
        _selectedMonth.value = month
    }

    /**
     * Update year type preference (Calendar vs Financial).
     */
    fun updateYearType(yearType: YearType) {
        viewModelScope.launch {
            preferencesRepository.setYearType(yearType)
        }
    }

    // Combined dashboard state
    val dashboardState: StateFlow<DashboardUiState> = combine(
        combine(
            expenseRepository.getAllExpenses(),
            incomeRepository.getAllIncome(),
            categoryRepository.getAllCategories(),
            accountRepository.getAllAccounts(),
            budgetRepository.getAllBudgets()
        ) { expenses, incomes, categories, accounts, allBudgets ->
            arrayOf(expenses, incomes, categories, accounts, allBudgets)
        },
        combine(
            preferencesRepository.getYearType(),
            _selectedMonth,
            _selectedYear
        ) { yearType, selectedMonth, selectedYear ->
            Triple(yearType, selectedMonth, selectedYear)
        }
    ) { dataArray, selectionData ->
        @Suppress("UNCHECKED_CAST")
        val expenses = dataArray[0] as List<Expense>

        @Suppress("UNCHECKED_CAST")
        val incomes = dataArray[1] as List<Income>

        @Suppress("UNCHECKED_CAST")
        val categories = dataArray[2] as List<Category>

        @Suppress("UNCHECKED_CAST")
        val accounts = dataArray[3] as List<Account>

        @Suppress("UNCHECKED_CAST")
        val allBudgets = dataArray[4] as List<Budget>

        val (yearType, selectedMonth, selectedYear) = selectionData

        // Calculate selected month boundaries
        val monthStart = getMonthStartMillis(selectedYear, selectedMonth)
        val monthEnd = getMonthEndMillis(selectedYear, selectedMonth)

        // Filter transactions for selected month
        val monthExpenses = expenses.filter { it.date in monthStart..monthEnd }
        val monthIncome = incomes.filter { it.date in monthStart..monthEnd }
        val monthExpenseTotal = monthExpenses.sumOf { it.amount }
        val monthIncomeTotal = monthIncome.sumOf { it.amount }
        val monthNetBalance = monthIncomeTotal - monthExpenseTotal

        // Calculate previous month for comparison
        val prevCalendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, selectedYear)
            set(Calendar.MONTH, selectedMonth - 1)
            add(Calendar.MONTH, -1)
        }
        val prevYear = prevCalendar.get(Calendar.YEAR)
        val prevMonth = prevCalendar.get(Calendar.MONTH) + 1
        val prevMonthStart = getMonthStartMillis(prevYear, prevMonth)
        val prevMonthEnd = getMonthEndMillis(prevYear, prevMonth)
        val prevMonthExpenses = expenses.filter { it.date in prevMonthStart..prevMonthEnd }
        val prevMonthIncome = incomes.filter { it.date in prevMonthStart..prevMonthEnd }
        val prevMonthExpenseTotal = prevMonthExpenses.sumOf { it.amount }
        val prevMonthIncomeTotal = prevMonthIncome.sumOf { it.amount }
        val prevMonthNetBalance = prevMonthIncomeTotal - prevMonthExpenseTotal

        // Month percentage changes
        val monthExpenseChange = calculatePercentageChange(prevMonthExpenseTotal, monthExpenseTotal)
        val monthIncomeChange = calculatePercentageChange(prevMonthIncomeTotal, monthIncomeTotal)
        val monthBalanceChange = calculatePercentageChange(prevMonthNetBalance, monthNetBalance)

        // Calculate full year boundaries based on selected month's year
        val (ytdStartYear, ytdStartMonth) = yearType.getYearStart(selectedYear, selectedMonth)
        val ytdStart = getMonthStartMillis(ytdStartYear, ytdStartMonth)

        // Calculate year end based on year type
        val ytdEndYear: Int
        val ytdEndMonth: Int
        when (yearType) {
            YearType.CALENDAR -> {
                ytdEndYear = selectedYear
                ytdEndMonth = 12 // December
            }

            YearType.FINANCIAL -> {
                // Financial year ends in March of next year
                if (selectedMonth >= 4) {
                    // If selected month is Apr-Dec, FY ends in March of next year
                    ytdEndYear = selectedYear + 1
                    ytdEndMonth = 3 // March
                } else {
                    // If selected month is Jan-Mar, FY ends in March of same year
                    ytdEndYear = selectedYear
                    ytdEndMonth = 3 // March
                }
            }
        }
        val ytdEnd = getMonthEndMillis(ytdEndYear, ytdEndMonth)

        // Filter transactions for full year
        val ytdExpenses = expenses.filter { it.date in ytdStart..ytdEnd }
        val ytdIncome = incomes.filter { it.date in ytdStart..ytdEnd }
        val ytdExpenseTotal = ytdExpenses.sumOf { it.amount }
        val ytdIncomeTotal = ytdIncome.sumOf { it.amount }
        val ytdNetBalance = ytdIncomeTotal - ytdExpenseTotal

        // Calculate previous year for comparison
        val prevYtdStart = getMonthStartMillis(ytdStartYear - 1, ytdStartMonth)
        val prevYtdEnd = getMonthEndMillis(ytdEndYear - 1, ytdEndMonth)
        val prevYtdExpenses = expenses.filter { it.date in prevYtdStart..prevYtdEnd }
        val prevYtdIncome = incomes.filter { it.date in prevYtdStart..prevYtdEnd }
        val prevYtdExpenseTotal = prevYtdExpenses.sumOf { it.amount }
        val prevYtdIncomeTotal = prevYtdIncome.sumOf { it.amount }
        val prevYtdNetBalance = prevYtdIncomeTotal - prevYtdExpenseTotal

        // YTD percentage changes
        val ytdExpenseChange = calculatePercentageChange(prevYtdExpenseTotal, ytdExpenseTotal)
        val ytdIncomeChange = calculatePercentageChange(prevYtdIncomeTotal, ytdIncomeTotal)
        val ytdBalanceChange = calculatePercentageChange(prevYtdNetBalance, ytdNetBalance)

        // Get recent 5 transactions from selected month
        val recentTransactions = buildRecentTransactions(monthExpenses, monthIncome).take(5)

        // Get top 3 categories for selected month
        val topCategories = getTopCategories(monthExpenses, categories)

        // Get budgets for selected month
        val monthBudgets =
            allBudgets.filter { it.month == selectedMonth && it.year == selectedYear }
        val budgetsWithProgress = monthBudgets.map { budget ->
            val spent = calculateSpentForBudget(budget, monthExpenses)
            val category = budget.categoryId?.let { id -> categories.find { it.id == id } }
            val progress = budget.calculateProgress(spent)

            BudgetWithProgress(
                budget = budget,
                category = category,
                currentSpent = spent,
                progress = progress,
                shouldNotify75 = budget.shouldNotify75(spent),
                shouldNotify100 = budget.shouldNotify100(spent)
            )
        }.sortedByDescending { it.progress }

        DashboardUiState.Success(
            financialSummary = FinancialSummary(
                selectedMonth = selectedMonth,
                selectedYear = selectedYear,
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
                yearType = yearType
            ),
            recentTransactions = recentTransactions,
            topCategories = topCategories,
            budgets = budgetsWithProgress,
            allCategories = categories,
            allAccounts = accounts,
            hasTransactions = expenses.isNotEmpty() || incomes.isNotEmpty()
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardUiState.Loading
        )

    /**
     * Refresh dashboard data.
     * Note: Room Flows automatically update when data changes, so manual refresh is not needed.
     * This method is kept for compatibility but does nothing.
     */
    fun refresh() {
        // No-op: Room Flows provide automatic real-time updates
    }

    /**
     * Calculate percentage change between two values
     */
    private fun calculatePercentageChange(previous: Long, current: Long): Float {
        if (previous == 0L) {
            return if (current > 0) 100f else 0f
        }
        return ((current - previous).toFloat() / previous.toFloat()) * 100f
    }

    /**
     * Build list of recent transactions combining expenses and income
     */
    private fun buildRecentTransactions(
        expenses: List<Expense>,
        incomes: List<Income>
    ): List<RecentTransaction> {
        val expenseTransactions = expenses.map { expense ->
            RecentTransaction.ExpenseTransaction(expense)
        }
        val incomeTransactions = incomes.map { income ->
            RecentTransaction.IncomeTransaction(income)
        }
        return (expenseTransactions + incomeTransactions)
            .sortedByDescending {
                when (it) {
                    is RecentTransaction.ExpenseTransaction -> it.expense.date
                    is RecentTransaction.IncomeTransaction -> it.income.date
                }
            }
    }

    /**
     * Get all spending categories for current month, sorted by amount
     */
    private fun getTopCategories(
        expenses: List<Expense>,
        categories: List<Category>
    ): List<CategorySpending> {
        val categoryMap = categories.associateBy { it.id }

        return expenses
            .groupBy { it.categoryId }
            .mapNotNull { (categoryId, expenses) ->
                categoryId?.let { id ->
                    categoryMap[id]?.let { category ->
                        CategorySpending(
                            category = category,
                            totalAmount = expenses.sumOf { it.amount },
                            transactionCount = expenses.size
                        )
                    }
                }
            }
            .sortedByDescending { it.totalAmount }
    }

    /**
     * Get start of month timestamp for given year and month.
     * Month is 1-indexed (1=Jan, 12=Dec).
     */
    private fun getMonthStartMillis(year: Int, month: Int): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1) // Calendar uses 0-indexed months
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    /**
     * Get end of month timestamp for given year and month.
     * Month is 1-indexed (1=Jan, 12=Dec).
     */
    private fun getMonthEndMillis(year: Int, month: Int): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1) // Calendar uses 0-indexed months
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return calendar.timeInMillis
    }

    /**
     * Calculate spent amount for a budget
     */
    private fun calculateSpentForBudget(budget: Budget, monthExpenses: List<Expense>): Long {
        return if (budget.categoryId != null) {
            monthExpenses
                .filter { it.categoryId == budget.categoryId }
                .sumOf { it.amount }
        } else {
            // Overall budget includes all expenses
            monthExpenses.sumOf { it.amount }
        }
    }
}

/**
 * UI state for dashboard screen
 */
sealed interface DashboardUiState {
    data object Loading : DashboardUiState
    data class Success(
        val financialSummary: FinancialSummary,
        val recentTransactions: List<RecentTransaction>,
        val topCategories: List<CategorySpending>,
        val budgets: List<BudgetWithProgress>,
        val allCategories: List<Category>,
        val allAccounts: List<Account>,
        val hasTransactions: Boolean
    ) : DashboardUiState

    data class Error(val message: String) : DashboardUiState
}

/**
 * Financial summary data with both month and YTD metrics.
 */
data class FinancialSummary(
    // Selected month info
    val selectedMonth: Int,        // 1-12
    val selectedYear: Int,

    // Selected month metrics
    val monthExpenses: Long,       // in paise
    val monthIncome: Long,         // in paise
    val monthNetBalance: Long,     // in paise
    val monthExpenseChange: Float, // % vs previous month
    val monthIncomeChange: Float,
    val monthBalanceChange: Float,

    // Year-to-date metrics (from year start to end of selected month)
    val ytdExpenses: Long,         // in paise
    val ytdIncome: Long,           // in paise
    val ytdNetBalance: Long,       // in paise
    val ytdExpenseChange: Float,   // % vs previous year same period
    val ytdIncomeChange: Float,
    val ytdBalanceChange: Float,

    // Year type used
    val yearType: YearType
)

/**
 * Recent transaction (can be expense or income)
 */
sealed class RecentTransaction {
    data class ExpenseTransaction(val expense: Expense) : RecentTransaction()
    data class IncomeTransaction(val income: Income) : RecentTransaction()
}

/**
 * Category spending data for top categories
 */
data class CategorySpending(
    val category: Category,
    val totalAmount: Long,      // in paise
    val transactionCount: Int
)
