package dev.lanthoor.spendly.ui.screens.dashboard

import dev.lanthoor.spendly.core.model.finance.BudgetWithProgress
import dev.lanthoor.spendly.core.model.finance.RecentTransaction
import dev.lanthoor.spendly.core.model.preferences.YearType
import dev.lanthoor.spendly.domain.model.Account
import dev.lanthoor.spendly.domain.model.Category

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

data class FinancialSummary(
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

data class CategorySpending(
    val category: Category,
    val totalAmount: Long,
    val transactionCount: Int
)
