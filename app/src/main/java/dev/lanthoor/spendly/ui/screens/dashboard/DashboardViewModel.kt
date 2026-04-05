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
import dev.lanthoor.spendly.ui.screens.dashboard.usecase.BuildDashboardSummaryUseCase
import dev.lanthoor.spendly.ui.screens.dashboard.usecase.DashboardSummaryInput
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
    private val preferencesRepository: PreferencesRepository,
    private val buildDashboardSummaryUseCase: BuildDashboardSummaryUseCase
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

        val summaryResult = buildDashboardSummaryUseCase.execute(
            DashboardSummaryInput(
                expenses = expenses,
                incomes = incomes,
                categories = categories,
                allBudgets = allBudgets,
                selectedMonth = selectedMonth,
                selectedYear = selectedYear,
                yearType = yearType
            )
        )

        DashboardUiState.Success(
            financialSummary = summaryResult.financialSummary,
            recentTransactions = summaryResult.recentTransactions,
            topCategories = summaryResult.topCategories,
            budgets = summaryResult.budgets,
            allCategories = categories,
            allAccounts = accounts,
            hasTransactions = summaryResult.hasTransactions
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
