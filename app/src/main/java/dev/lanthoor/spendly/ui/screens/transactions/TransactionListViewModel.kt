package dev.lanthoor.spendly.ui.screens.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.lanthoor.spendly.domain.model.Account
import dev.lanthoor.spendly.domain.model.Category
import dev.lanthoor.spendly.domain.model.Expense
import dev.lanthoor.spendly.domain.model.Income
import dev.lanthoor.spendly.domain.repository.AccountRepository
import dev.lanthoor.spendly.domain.repository.CategoryRepository
import dev.lanthoor.spendly.domain.repository.ExpenseRepository
import dev.lanthoor.spendly.domain.repository.IncomeRepository
import dev.lanthoor.spendly.ui.screens.dashboard.RecentTransaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * ViewModel for all transactions list screen with filtering capabilities.
 */
@HiltViewModel
class TransactionListViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val incomeRepository: IncomeRepository,
    private val categoryRepository: CategoryRepository,
    private val accountRepository: AccountRepository
) : ViewModel() {

    // Filter states
    private val _startDate = MutableStateFlow<Long?>(null)
    private val _endDate = MutableStateFlow<Long?>(null)
    private val _selectedType = MutableStateFlow<TransactionType>(TransactionType.ALL)
    private val _selectedCategories = MutableStateFlow<Set<Long>>(emptySet())

    val startDate: StateFlow<Long?> = _startDate.asStateFlow()
    val endDate: StateFlow<Long?> = _endDate.asStateFlow()
    val selectedType: StateFlow<TransactionType> = _selectedType.asStateFlow()
    val selectedCategories: StateFlow<Set<Long>> = _selectedCategories.asStateFlow()

    /**
     * Combined state with all transactions and filtering
     */
    val transactionListState: StateFlow<TransactionListUiState> = combine(
        combine(
            expenseRepository.getAllExpenses(),
            incomeRepository.getAllIncome(),
            categoryRepository.getAllCategories(),
            accountRepository.getAllAccounts()
        ) { expenses, incomes, categories, accounts ->
            arrayOf(expenses, incomes, categories, accounts)
        },
        combine(
            _startDate,
            _endDate,
            _selectedType,
            _selectedCategories
        ) { startDate, endDate, selectedType, selectedCategories ->
            arrayOf(startDate, endDate, selectedType, selectedCategories)
        }
    ) { dataArray, filterArray ->
        @Suppress("UNCHECKED_CAST")
        val expenses = dataArray[0] as List<Expense>

        @Suppress("UNCHECKED_CAST")
        val incomes = dataArray[1] as List<Income>

        @Suppress("UNCHECKED_CAST")
        val categories = dataArray[2] as List<Category>

        @Suppress("UNCHECKED_CAST")
        val accounts = dataArray[3] as List<Account>

        val startDate = filterArray[0] as Long?
        val endDate = filterArray[1] as Long?

        @Suppress("UNCHECKED_CAST")
        val selectedType = filterArray[2] as TransactionType

        @Suppress("UNCHECKED_CAST")
        val selectedCategories = filterArray[3] as Set<Long>

        // Apply date range filter
        val filteredExpenses = expenses.filter { expense ->
            val matchesDate = when {
                startDate != null && endDate != null -> expense.date in startDate..endDate
                startDate != null -> expense.date >= startDate
                endDate != null -> expense.date <= endDate
                else -> true
            }
            val matchesCategory = selectedCategories.isEmpty() ||
                    (expense.categoryId != null && expense.categoryId in selectedCategories)
            matchesDate && matchesCategory
        }

        val filteredIncomes = incomes.filter { income ->
            val matchesDate = when {
                startDate != null && endDate != null -> income.date in startDate..endDate
                startDate != null -> income.date >= startDate
                endDate != null -> income.date <= endDate
                else -> true
            }
            val matchesCategory = selectedCategories.isEmpty() ||
                    (income.categoryId != null && income.categoryId in selectedCategories)
            matchesDate && matchesCategory
        }

        // Apply type filter and build transaction list
        val filteredTransactions = buildTransactionList(
            expenses = if (selectedType == TransactionType.INCOME) emptyList() else filteredExpenses,
            incomes = if (selectedType == TransactionType.EXPENSE) emptyList() else filteredIncomes
        )

        // Build unfiltered transaction list (for category filtering in bottom sheet)
        val allTransactions = buildTransactionList(
            expenses = expenses,
            incomes = incomes
        )

        TransactionListUiState.Success(
            transactions = filteredTransactions,
            allTransactions = allTransactions,
            allCategories = categories,
            allAccounts = accounts,
            hasTransactions = expenses.isNotEmpty() || incomes.isNotEmpty()
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TransactionListUiState.Loading
        )

    /**
     * Build list of all transactions in reverse chronological order
     */
    private fun buildTransactionList(
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
     * Set date range filter
     */
    fun setDateRange(startDate: Long?, endDate: Long?) {
        _startDate.value = startDate
        _endDate.value = endDate
    }

    /**
     * Set transaction type filter
     */
    fun setTransactionType(type: TransactionType) {
        _selectedType.value = type
    }

    /**
     * Toggle category in filter
     */
    fun toggleCategory(categoryId: Long) {
        _selectedCategories.value = if (categoryId in _selectedCategories.value) {
            _selectedCategories.value - categoryId
        } else {
            _selectedCategories.value + categoryId
        }
    }

    /**
     * Clear all category filters
     */
    fun clearCategoryFilters() {
        _selectedCategories.value = emptySet()
    }

    /**
     * Clear all filters
     */
    fun clearAllFilters() {
        _startDate.value = null
        _endDate.value = null
        _selectedType.value = TransactionType.ALL
        _selectedCategories.value = emptySet()
    }

    /**
     * Check if any filters are active
     */
    fun hasActiveFilters(): Boolean {
        return _startDate.value != null ||
                _endDate.value != null ||
                _selectedType.value != TransactionType.ALL ||
                _selectedCategories.value.isNotEmpty()
    }

    /**
     * Refresh transaction data.
     * Note: Room Flows automatically update when data changes, so manual refresh is not needed.
     * This method is kept for compatibility but does nothing.
     */
    fun refresh() {
        // No-op: Room Flows provide automatic real-time updates
    }
}

/**
 * UI state for transaction list screen
 */
sealed interface TransactionListUiState {
    data object Loading : TransactionListUiState
    data class Success(
        val transactions: List<RecentTransaction>,
        val allTransactions: List<RecentTransaction>,
        val allCategories: List<Category>,
        val allAccounts: List<Account>,
        val hasTransactions: Boolean
    ) : TransactionListUiState

    data class Error(val message: String) : TransactionListUiState
}

/**
 * Transaction type filter
 */
enum class TransactionType {
    ALL,
    EXPENSE,
    INCOME
}
