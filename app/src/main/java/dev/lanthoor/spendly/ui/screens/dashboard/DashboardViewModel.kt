package dev.lanthoor.spendly.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.lanthoor.spendly.domain.model.Account
import dev.lanthoor.spendly.domain.model.Budget
import dev.lanthoor.spendly.domain.model.Category
import dev.lanthoor.spendly.domain.model.Expense
import dev.lanthoor.spendly.domain.model.Income
import dev.lanthoor.spendly.domain.repository.AccountRepository
import dev.lanthoor.spendly.domain.repository.BudgetRepository
import dev.lanthoor.spendly.domain.repository.CategoryRepository
import dev.lanthoor.spendly.domain.repository.ExpenseRepository
import dev.lanthoor.spendly.domain.repository.IncomeRepository
import dev.lanthoor.spendly.domain.repository.PreferencesRepository
import dev.lanthoor.spendly.core.model.preferences.YearType
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
        val monthStart = DashboardDateUtils.getMonthStartMillis(selectedYear, selectedMonth)
        val monthEnd = DashboardDateUtils.getMonthEndMillis(selectedYear, selectedMonth)

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
        val prevMonthStart = DashboardDateUtils.getMonthStartMillis(prevYear, prevMonth)
        val prevMonthEnd = DashboardDateUtils.getMonthEndMillis(prevYear, prevMonth)
        val prevMonthExpenses = expenses.filter { it.date in prevMonthStart..prevMonthEnd }
        val prevMonthIncome = incomes.filter { it.date in prevMonthStart..prevMonthEnd }
        val prevMonthExpenseTotal = prevMonthExpenses.sumOf { it.amount }
        val prevMonthIncomeTotal = prevMonthIncome.sumOf { it.amount }
        val prevMonthNetBalance = prevMonthIncomeTotal - prevMonthExpenseTotal

        // Month percentage changes
        val monthExpenseChange = DashboardCalculators.calculatePercentageChange(prevMonthExpenseTotal, monthExpenseTotal)
        val monthIncomeChange = DashboardCalculators.calculatePercentageChange(prevMonthIncomeTotal, monthIncomeTotal)
        val monthBalanceChange = DashboardCalculators.calculatePercentageChange(prevMonthNetBalance, monthNetBalance)

        // Calculate full year boundaries based on selected month's year
        val (ytdStartYear, ytdStartMonth) = yearType.getYearStart(selectedYear, selectedMonth)
        val ytdStart = DashboardDateUtils.getMonthStartMillis(ytdStartYear, ytdStartMonth)

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
        val ytdEnd = DashboardDateUtils.getMonthEndMillis(ytdEndYear, ytdEndMonth)

        // Filter transactions for full year
        val ytdExpenses = expenses.filter { it.date in ytdStart..ytdEnd }
        val ytdIncome = incomes.filter { it.date in ytdStart..ytdEnd }
        val ytdExpenseTotal = ytdExpenses.sumOf { it.amount }
        val ytdIncomeTotal = ytdIncome.sumOf { it.amount }
        val ytdNetBalance = ytdIncomeTotal - ytdExpenseTotal

        // Calculate previous year for comparison
        val prevYtdStart = DashboardDateUtils.getMonthStartMillis(ytdStartYear - 1, ytdStartMonth)
        val prevYtdEnd = DashboardDateUtils.getMonthEndMillis(ytdEndYear - 1, ytdEndMonth)
        val prevYtdExpenses = expenses.filter { it.date in prevYtdStart..prevYtdEnd }
        val prevYtdIncome = incomes.filter { it.date in prevYtdStart..prevYtdEnd }
        val prevYtdExpenseTotal = prevYtdExpenses.sumOf { it.amount }
        val prevYtdIncomeTotal = prevYtdIncome.sumOf { it.amount }
        val prevYtdNetBalance = prevYtdIncomeTotal - prevYtdExpenseTotal

        // YTD percentage changes
        val ytdExpenseChange = DashboardCalculators.calculatePercentageChange(prevYtdExpenseTotal, ytdExpenseTotal)
        val ytdIncomeChange = DashboardCalculators.calculatePercentageChange(prevYtdIncomeTotal, ytdIncomeTotal)
        val ytdBalanceChange = DashboardCalculators.calculatePercentageChange(prevYtdNetBalance, ytdNetBalance)

        // Get recent 5 transactions from selected month
        val recentTransactions = DashboardCalculators.buildRecentTransactions(monthExpenses, monthIncome).take(5)

        // Get top 3 categories for selected month
        val topCategories = DashboardCalculators.getTopCategories(monthExpenses, categories)

        // Get budgets for selected month
        val budgetsWithProgress = DashboardCalculators.toBudgetsWithProgress(
            allBudgets = allBudgets,
            selectedMonth = selectedMonth,
            selectedYear = selectedYear,
            categories = categories,
            monthExpenses = monthExpenses
        )

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

}
