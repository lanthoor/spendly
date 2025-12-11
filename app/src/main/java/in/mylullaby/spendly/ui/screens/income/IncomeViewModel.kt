package `in`.mylullaby.spendly.ui.screens.income

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
import `in`.mylullaby.spendly.utils.CurrencyUtils
import `in`.mylullaby.spendly.utils.IncomeSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing income-related screens (list, add, edit).
 * Handles state management, validation, and repository interactions.
 */
@HiltViewModel
class IncomeViewModel @Inject constructor(
    private val incomeRepository: IncomeRepository,
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val accountRepository: AccountRepository
) : ViewModel() {

    // UI State for income list screen
    private val _uiState = MutableStateFlow<IncomeListUiState>(IncomeListUiState.Loading)
    val uiState: StateFlow<IncomeListUiState> = _uiState.asStateFlow()

    // Form state for add/edit screens
    private val _formState = MutableStateFlow(IncomeFormState())
    val formState: StateFlow<IncomeFormState> = _formState.asStateFlow()

    // Filter state
    private val _filters = MutableStateFlow(IncomeFilters())
    val filters: StateFlow<IncomeFilters> = _filters.asStateFlow()

    // Income categories
    val incomeCategories: StateFlow<List<Category>> = categoryRepository.getIncomeCategories()
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // All expenses (for refund linking)
    val expenses: StateFlow<List<Expense>> = expenseRepository.getAllExpenses()
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // All accounts
    val accounts: StateFlow<List<Account>> = accountRepository.getAllAccounts()
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        loadIncomes()
    }

    /**
     * Loads incomes based on current filters
     */
    fun loadIncomes() {
        viewModelScope.launch {
            _uiState.value = IncomeListUiState.Loading

            try {
                val filters = _filters.value

                // Determine which repository method to use based on active filters
                val incomesFlow = when {
                    // Date range filter
                    filters.startDate != null && filters.endDate != null -> {
                        incomeRepository.getIncomeByDateRange(filters.startDate, filters.endDate)
                    }
                    // Source filter (single source for now, can extend to multiple)
                    filters.sources.isNotEmpty() -> {
                        val source = filters.sources.first()
                        incomeRepository.getIncomeBySource(source)
                    }
                    // No filters - get all incomes
                    else -> incomeRepository.getAllIncome()
                }

                // Combine with total income calculation
                combine(
                    incomesFlow,
                    calculateTotalIncome()
                ) { incomes, total ->
                    IncomeListUiState.Success(
                        incomes = applyClientSideFilters(incomes),
                        filters = filters,
                        totalIncome = CurrencyUtils.formatPaise(total)
                    )
                }.catch { error ->
                    _uiState.value = IncomeListUiState.Error(
                        message = error.message ?: "Failed to load income"
                    )
                }.collect { state ->
                    _uiState.value = state
                }
            } catch (e: Exception) {
                _uiState.value = IncomeListUiState.Error(
                    message = e.message ?: "Failed to load income"
                )
            }
        }
    }

    /**
     * Apply client-side filters when multiple filter types are active
     */
    private fun applyClientSideFilters(incomes: List<Income>): List<Income> {
        val filters = _filters.value
        var filtered = incomes

        // Apply date range if set
        if (filters.startDate != null && filters.endDate != null) {
            filtered = filtered.filter { it.date in filters.startDate..filters.endDate }
        }

        // Apply source filter if set
        if (filters.sources.isNotEmpty()) {
            filtered = filtered.filter { it.source in filters.sources }
        }

        // Apply recurring filter if set
        if (filters.recurringOnly) {
            filtered = filtered.filter { it.isRecurring }
        }

        return filtered
    }

    /**
     * Calculates total income based on current filters
     */
    private fun calculateTotalIncome(): Flow<Long> {
        val filters = _filters.value
        return if (filters.startDate != null && filters.endDate != null) {
            incomeRepository.getTotalIncomeInRange(filters.startDate, filters.endDate)
        } else {
            // Calculate from all incomes
            incomeRepository.getAllIncome()
                .catch { emit(emptyList()) }
                .map { incomes ->
                    incomes.sumOf { it.amount }
                }
        }
    }

    /**
     * Loads a specific income for editing
     */
    fun loadIncomeById(id: Long) {
        viewModelScope.launch {
            try {
                incomeRepository.getIncomeById(id).collect { income ->
                    if (income != null) {
                        // Load category if present
                        val category = income.categoryId?.let { categoryId ->
                            incomeCategories.value.find { it.id == categoryId }
                        }

                        _formState.update {
                            IncomeFormState(
                                id = income.id,
                                amount = CurrencyUtils.paiseToRupeeString(income.amount),
                                selectedCategory = category,
                                accountId = income.accountId,
                                source = income.source,
                                date = income.date,
                                description = income.description,
                                isRecurring = income.isRecurring,
                                linkedExpenseId = income.linkedExpenseId,
                                createdAt = income.createdAt,
                                isEditMode = true
                            )
                        }
                    } else {
                        _formState.update {
                            it.copy(submitError = "Income not found")
                        }
                    }
                }
            } catch (e: Exception) {
                _formState.update {
                    it.copy(submitError = e.message ?: "Failed to load income")
                }
            }
        }
    }

    /**
     * Updates a form field
     */
    fun updateFormField(field: IncomeFormField, value: Any) {
        _formState.update { currentState ->
            when (field) {
                IncomeFormField.AMOUNT -> {
                    val amountStr = value as String
                    currentState.copy(
                        amount = amountStr,
                        amountError = validateAmount(amountStr)
                    )
                }
                IncomeFormField.CATEGORY -> currentState.copy(selectedCategory = value as Category)
                IncomeFormField.ACCOUNT_ID -> currentState.copy(accountId = value as Long)
                IncomeFormField.SOURCE -> currentState.copy(source = value as IncomeSource)
                IncomeFormField.DATE -> currentState.copy(date = value as Long)
                IncomeFormField.DESCRIPTION -> {
                    val descStr = value as String
                    currentState.copy(
                        description = descStr,
                        descriptionError = validateDescription(descStr)
                    )
                }
                IncomeFormField.IS_RECURRING -> currentState.copy(isRecurring = value as Boolean)
                IncomeFormField.LINKED_EXPENSE_ID -> {
                    val expenseId = value as Long
                    currentState.copy(linkedExpenseId = if (expenseId == 0L) null else expenseId)
                }
            }
        }
    }

    /**
     * Validates the entire form
     */
    fun validateForm(): Boolean {
        val state = _formState.value
        val amountError = validateAmount(state.amount)
        val descError = validateDescription(state.description)

        _formState.update {
            it.copy(
                amountError = amountError,
                descriptionError = descError
            )
        }

        return amountError == null && descError == null
    }

    /**
     * Validates amount field
     */
    private fun validateAmount(amount: String): String? {
        return when {
            amount.isBlank() -> "Amount is required"
            amount.toDoubleOrNull() == null -> "Invalid amount format"
            amount.toDouble() <= 0 -> "Amount must be greater than 0"
            else -> null
        }
    }

    /**
     * Validates description field
     */
    private fun validateDescription(description: String): String? {
        return when {
            description.isBlank() -> "Description is required"
            description.length < 3 -> "Description must be at least 3 characters"
            description.length > 200 -> "Description must not exceed 200 characters"
            else -> null
        }
    }

    /**
     * Saves the income (create or update)
     */
    suspend fun saveIncome(): Result<Long> {
        if (!validateForm()) {
            return Result.failure(Exception("Please fix validation errors"))
        }

        _formState.update { it.copy(isSubmitting = true, submitError = null) }

        return try {
            val state = _formState.value
            val amountInPaise = CurrencyUtils.parseRupeesToPaise(state.amount)
            val currentTime = System.currentTimeMillis()

            val income = Income(
                id = state.id,
                amount = amountInPaise,
                categoryId = state.selectedCategory?.id,
                accountId = state.accountId,
                source = state.source,
                date = state.date,
                description = state.description.trim(),
                isRecurring = state.isRecurring,
                linkedExpenseId = state.linkedExpenseId,
                createdAt = state.createdAt ?: currentTime,
                modifiedAt = currentTime
            )

            val result = if (state.isEditMode) {
                incomeRepository.updateIncome(income)
                Result.success(income.id)
            } else {
                val id = incomeRepository.insertIncome(income)
                Result.success(id)
            }

            _formState.update { it.copy(isSubmitting = false) }
            result
        } catch (e: Exception) {
            _formState.update {
                it.copy(
                    isSubmitting = false,
                    submitError = e.message ?: "Failed to save income"
                )
            }
            Result.failure(e)
        }
    }

    /**
     * Deletes an income
     */
    suspend fun deleteIncome(id: Long): Result<Unit> {
        return try {
            val income = Income(
                id = id,
                amount = 0,
                categoryId = null,
                accountId = Account.DEFAULT_ACCOUNT_ID,
                source = IncomeSource.OTHER,
                date = 0,
                description = "",
                isRecurring = false,
                linkedExpenseId = null,
                createdAt = 0,
                modifiedAt = 0
            )
            incomeRepository.deleteIncome(income)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Applies filters and reloads incomes
     */
    fun applyFilters(filters: IncomeFilters) {
        _filters.value = filters
        loadIncomes()
    }

    /**
     * Clears all filters
     */
    fun clearFilters() {
        _filters.value = IncomeFilters()
        loadIncomes()
    }

    /**
     * Resets form state (for new income entry)
     */
    fun resetForm() {
        _formState.value = IncomeFormState()
    }
}

/**
 * UI state for income list screen
 */
sealed interface IncomeListUiState {
    data object Loading : IncomeListUiState
    data class Success(
        val incomes: List<Income>,
        val filters: IncomeFilters,
        val totalIncome: String
    ) : IncomeListUiState
    data class Error(val message: String) : IncomeListUiState
}

/**
 * Form state for add/edit income screens
 */
data class IncomeFormState(
    val id: Long = 0,
    val amount: String = "",
    val amountError: String? = null,
    val selectedCategory: Category? = null,
    val accountId: Long = Account.DEFAULT_ACCOUNT_ID,
    val source: IncomeSource = IncomeSource.SALARY, // DEPRECATED - use selectedCategory
    val date: Long = System.currentTimeMillis(),
    val description: String = "",
    val descriptionError: String? = null,
    val isRecurring: Boolean = false,
    val linkedExpenseId: Long? = null,
    val createdAt: Long? = null,
    val isEditMode: Boolean = false,
    val isSubmitting: Boolean = false,
    val submitError: String? = null
)

/**
 * Filter state for income list
 */
data class IncomeFilters(
    val startDate: Long? = null,
    val endDate: Long? = null,
    val sources: Set<IncomeSource> = emptySet(),
    val recurringOnly: Boolean = false
)

/**
 * Form fields enum for type-safe updates
 */
enum class IncomeFormField {
    AMOUNT,
    CATEGORY,
    ACCOUNT_ID,
    SOURCE, // DEPRECATED - use CATEGORY
    DATE,
    DESCRIPTION,
    IS_RECURRING,
    LINKED_EXPENSE_ID
}
