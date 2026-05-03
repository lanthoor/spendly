package dev.lanthoor.spendly.ui.screens.transactions

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.lanthoor.spendly.core.model.finance.RecentTransaction
import dev.lanthoor.spendly.core.model.preferences.AiEnrichmentSettings
import dev.lanthoor.spendly.core.model.preferences.AiModelAvailability
import dev.lanthoor.spendly.core.model.preferences.AiPromptVersion
import dev.lanthoor.spendly.domain.model.Account
import dev.lanthoor.spendly.domain.model.Category
import dev.lanthoor.spendly.domain.model.Expense
import dev.lanthoor.spendly.domain.model.Income
import dev.lanthoor.spendly.domain.model.TransactionAiEnrichment
import dev.lanthoor.spendly.domain.repository.AccountRepository
import dev.lanthoor.spendly.domain.repository.CategoryRepository
import dev.lanthoor.spendly.domain.repository.ExpenseRepository
import dev.lanthoor.spendly.domain.repository.IncomeRepository
import dev.lanthoor.spendly.domain.repository.PreferencesRepository
import dev.lanthoor.spendly.domain.repository.TransactionAiEnrichmentRepository
import dev.lanthoor.spendly.domain.usecase.transactions.EnrichSmsTransactionsResult
import dev.lanthoor.spendly.domain.usecase.transactions.EnrichSmsTransactionsUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for all transactions list screen with filtering capabilities.
 */
@HiltViewModel
class TransactionListViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val incomeRepository: IncomeRepository,
    private val categoryRepository: CategoryRepository,
    private val accountRepository: AccountRepository,
    private val preferencesRepository: PreferencesRepository,
    private val enrichmentRepository: TransactionAiEnrichmentRepository,
    private val enrichSmsTransactionsUseCase: EnrichSmsTransactionsUseCase
) : ViewModel() {
    companion object {
        private const val TAG = "TransactionListVM"
    }

    // Filter states
    private val _startDate = MutableStateFlow<Long?>(null)
    private val _endDate = MutableStateFlow<Long?>(null)
    private val _selectedType = MutableStateFlow<TransactionType>(TransactionType.ALL)
    private val _selectedCategories = MutableStateFlow<Set<Long>>(emptySet())

    val startDate: StateFlow<Long?> = _startDate.asStateFlow()
    val endDate: StateFlow<Long?> = _endDate.asStateFlow()
    val selectedType: StateFlow<TransactionType> = _selectedType.asStateFlow()
    val selectedCategories: StateFlow<Set<Long>> = _selectedCategories.asStateFlow()
    private val _isEnrichmentRunning = MutableStateFlow(false)
    val isEnrichmentRunning: StateFlow<Boolean> = _isEnrichmentRunning.asStateFlow()

    private val _enrichmentResultEvents = MutableSharedFlow<EnrichSmsTransactionsResult>()
    val enrichmentResultEvents = _enrichmentResultEvents.asSharedFlow()

    val aiSettings: StateFlow<AiEnrichmentSettings> = preferencesRepository.getAiEnrichmentSettings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AiEnrichmentSettings(
                enabled = true,
                availability = AiModelAvailability.UNKNOWN,
                baseModelName = null,
                lastAvailabilityCheckAt = null,
                lastErrorCode = null,
                promptVersion = AiPromptVersion.CURRENT,
                batchSize = 20
            )
        )

    init {
        viewModelScope.launch {
            aiSettings.collect { settings ->
                Log.d(
                    TAG,
                    "aiSettings changed: enabled=${settings.enabled}, " +
                            "availability=${settings.availability}, baseModel=${settings.baseModelName}, " +
                            "lastError=${settings.lastErrorCode}, checkedAt=${settings.lastAvailabilityCheckAt}, " +
                            "batchSize=${settings.batchSize}, promptVersion=${settings.promptVersion}"
                )
            }
        }

        viewModelScope.launch {
            Log.d(TAG, "init: refreshing model availability")
            runCatching { enrichSmsTransactionsUseCase.refreshModelAvailability() }
                .onFailure { e ->
                    Log.w(TAG, "init: refreshModelAvailability failed", e)
                }
        }
    }

    // Data carrier for combined repository flows
    private data class TransactionData(
        val expenses: List<Expense>,
        val incomes: List<Income>,
        val categories: List<Category>,
        val accounts: List<Account>,
        val enrichments: List<TransactionAiEnrichment>
    )

    // Data carrier for combined filter flows
    private data class FilterData(
        val startDate: Long?,
        val endDate: Long?,
        val selectedType: TransactionType,
        val selectedCategories: Set<Long>
    )

    /**
     * Combined state with all transactions and filtering
     */
    val transactionListState: StateFlow<<TransactionTransactionListUiState> = combine(
        combine(
            expenseRepository.getAllExpenses(),
            incomeRepository.getAllIncome(),
            categoryRepository.getAllCategories(),
            accountRepository.getAllAccounts(),
            enrichmentRepository.observeAll()
        ) { expenses, incomes, categories, accounts, enrichments ->
            TransactionData(expenses, incomes, categories, accounts, enrichments)
        }.distinctUntilChanged(),
        combine(
            _startDate,
            _endDate,
            _selectedType,
            _selectedCategories
        ) { startDate, endDate, selectedType, selectedCategories ->
            FilterData(startDate, endDate, selectedType, selectedCategories)
        }.distinctUntilChanged()
    ) { data, filters ->
        // Apply date range filter
        val filteredExpenses = data.expenses.filter { expense ->
            val matchesDate = when {
                filters.startDate != null && filters.endDate != null -> expense.date in filters.startDate..filters.endDate
                filters.startDate != null -> expense.date >= filters.startDate
                filters.endDate != null -> expense.date <= filters.endDate
                else -> true
            }
            val matchesCategory = filters.selectedCategories.isEmpty() ||
                    (expense.categoryId != null && expense.categoryId in filters.selectedCategories)
            matchesDate && matchesCategory
        }

        val filteredIncomes = data.incomes.filter { income ->
            val matchesDate = when {
                filters.startDate != null && filters.endDate != null -> income.date in filters.startDate..filters.endDate
                filters.startDate != null -> income.date >= filters.startDate
                filters.endDate != null -> income.date <= filters.endDate
                else -> true
            }
            val matchesCategory = filters.selectedCategories.isEmpty() ||
                    (income.categoryId != null && income.categoryId in filters.selectedCategories)
            matchesDate && matchesCategory
        }

        // Apply type filter and build transaction list
        val filteredTransactions = buildTransactionList(
            expenses = if (filters.selectedType == TransactionType.INCOME) emptyList() else filteredExpenses,
            incomes = if (filters.selectedType == TransactionType.EXPENSE) emptyList() else filteredIncomes
        )

        // Build unfiltered transaction list (for category filtering in bottom sheet)
        val allTransactions = buildTransactionList(
            expenses = data.expenses,
            incomes = data.incomes
        )

        TransactionListUiState.Success(
            transactions = filteredTransactions,
            allTransactions = allTransactions,
            allCategories = data.categories,
            allAccounts = data.accounts,
            enrichmentByKey = data.enrichments.associateBy { "${it.transactionType.name}:${it.transactionId}" },
            hasTransactions = data.expenses.isNotEmpty() || data.incomes.isNotEmpty()
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

    fun enrichTransactions(transactions: List<RecentTransaction>) {
        if (_isEnrichmentRunning.value) {
            Log.d(TAG, "enrichTransactions: skipped, already running")
            return
        }

        val settings = aiSettings.value
        Log.d(
            TAG,
            "enrichTransactions: requested with visibleTransactions=${transactions.size}, " +
                    "availability=${settings.availability}, lastError=${settings.lastErrorCode}"
        )
        if (settings.availability != AiModelAvailability.AVAILABLE) {
            Log.d(TAG, "enrichTransactions: blocked, model not available")
            return
        }
        if (isQuotaOrRateLimited(settings.lastErrorCode)) {
            Log.d(TAG, "enrichTransactions: blocked, quota/rate limited")
            return
        }

        viewModelScope.launch {
            _isEnrichmentRunning.value = true
            try {
                val expenseIds = transactions.mapNotNull { transaction ->
                    when (transaction) {
                        is RecentTransaction.ExpenseTransaction -> transaction.expense.id
                        is RecentTransaction.IncomeTransaction -> null
                    }
                }
                val incomeIds = transactions.mapNotNull { transaction ->
                    when (transaction) {
                        is RecentTransaction.ExpenseTransaction -> null
                        is RecentTransaction.IncomeTransaction -> transaction.income.id
                    }
                }

                val result = enrichSmsTransactionsUseCase.runForTransactionIds(expenseIds, incomeIds)
                Log.d(
                    TAG,
                    "enrichTransactions: completed attempted=${result.attempted}, " +
                            "enriched=${result.enriched}, failed=${result.failed}, skipped=${result.skipped}"
                )
                _enrichmentResultEvents.emit(result)
            } finally {
                _isEnrichmentRunning.value = false
                Log.d(TAG, "enrichTransactions: finished")
            }
        }
    }

    private fun isQuotaOrRateLimited(errorCode: String?): Boolean {
        val code = errorCode ?: return false
        return code == "QUOTA_EXCEEDED" || code == "RATE_LIMIT_EXCEEDED"
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
        val enrichmentByKey: Map<String, TransactionAiEnrichment>,
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
