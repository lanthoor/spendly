package `in`.mylullaby.spendly.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.mylullaby.spendly.domain.model.Account
import `in`.mylullaby.spendly.domain.model.Category
import `in`.mylullaby.spendly.domain.model.Expense
import `in`.mylullaby.spendly.domain.model.Income
import `in`.mylullaby.spendly.domain.repository.AccountRepository
import `in`.mylullaby.spendly.domain.repository.CategoryRepository
import `in`.mylullaby.spendly.domain.repository.ExpenseRepository
import `in`.mylullaby.spendly.domain.repository.IncomeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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
    private val accountRepository: AccountRepository
) : ViewModel() {

    // Combined dashboard state
    val dashboardState: StateFlow<DashboardUiState> = combine(
        expenseRepository.getAllExpenses(),
        incomeRepository.getAllIncome(),
        categoryRepository.getAllCategories(),
        accountRepository.getAllAccounts()
    ) { expenses, incomes, categories, accounts ->
        val now = System.currentTimeMillis()
        val currentMonthStart = getMonthStartMillis(now)
        val currentMonthEnd = getMonthEndMillis(now)
        val previousMonthStart = getMonthStartMillis(currentMonthStart - 1)
        val previousMonthEnd = getMonthEndMillis(currentMonthStart - 1)

        // Current month totals
        val currentMonthExpenses = expenses.filter { it.date in currentMonthStart..currentMonthEnd }
        val currentMonthIncome = incomes.filter { it.date in currentMonthStart..currentMonthEnd }
        val currentExpenseTotal = currentMonthExpenses.sumOf { it.amount }
        val currentIncomeTotal = currentMonthIncome.sumOf { it.amount }
        val currentNetBalance = currentIncomeTotal - currentExpenseTotal

        // Previous month totals
        val previousMonthExpenses = expenses.filter { it.date in previousMonthStart..previousMonthEnd }
        val previousMonthIncome = incomes.filter { it.date in previousMonthStart..previousMonthEnd }
        val previousExpenseTotal = previousMonthExpenses.sumOf { it.amount }
        val previousIncomeTotal = previousMonthIncome.sumOf { it.amount }
        val previousNetBalance = previousIncomeTotal - previousExpenseTotal

        // Calculate percentage changes
        val expenseChange = calculatePercentageChange(previousExpenseTotal, currentExpenseTotal)
        val incomeChange = calculatePercentageChange(previousIncomeTotal, currentIncomeTotal)
        val netBalanceChange = calculatePercentageChange(previousNetBalance, currentNetBalance)

        // Get recent 5 transactions (combined expenses + income, sorted by date DESC)
        val recentTransactions = buildRecentTransactions(expenses, incomes).take(5)

        // Get top 3 spending categories
        val topCategories = getTopCategories(currentMonthExpenses, categories)

        DashboardUiState.Success(
            financialSummary = FinancialSummary(
                totalExpenses = currentExpenseTotal,
                totalIncome = currentIncomeTotal,
                netBalance = currentNetBalance,
                expenseChange = expenseChange,
                incomeChange = incomeChange,
                netBalanceChange = netBalanceChange
            ),
            recentTransactions = recentTransactions,
            topCategories = topCategories,
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
     * Refresh dashboard data
     */
    fun refresh() {
        // Data automatically refreshes via Flow
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
     * Get top 3 spending categories for current month
     */
    private fun getTopCategories(
        expenses: List<Expense>,
        categories: List<Category>
    ): List<CategorySpending> {
        val categoryMap = categories.associateBy { it.id }
        return expenses
            .filter { it.categoryId != null }
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
            .take(3)
    }

    /**
     * Get start of month timestamp
     */
    private fun getMonthStartMillis(timestamp: Long): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    /**
     * Get end of month timestamp
     */
    private fun getMonthEndMillis(timestamp: Long): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return calendar.timeInMillis
    }
}

/**
 * UI state for dashboard screen
 */
sealed class DashboardUiState {
    data object Loading : DashboardUiState()
    data class Success(
        val financialSummary: FinancialSummary,
        val recentTransactions: List<RecentTransaction>,
        val topCategories: List<CategorySpending>,
        val allCategories: List<Category>,
        val allAccounts: List<Account>,
        val hasTransactions: Boolean
    ) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}

/**
 * Financial summary data
 */
data class FinancialSummary(
    val totalExpenses: Long,  // in paise
    val totalIncome: Long,     // in paise
    val netBalance: Long,      // in paise
    val expenseChange: Float,  // percentage change from previous month
    val incomeChange: Float,   // percentage change from previous month
    val netBalanceChange: Float // percentage change from previous month
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
