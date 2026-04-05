package dev.lanthoor.spendly.ui.screens.income

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.lanthoor.spendly.core.model.finance.IncomeSource
import dev.lanthoor.spendly.domain.model.Category
import dev.lanthoor.spendly.domain.model.Expense
import dev.lanthoor.spendly.domain.model.Account
import dev.lanthoor.spendly.domain.repository.AccountRepository
import dev.lanthoor.spendly.domain.repository.CategoryRepository
import dev.lanthoor.spendly.domain.repository.ExpenseRepository
import dev.lanthoor.spendly.domain.repository.IncomeRepository
import dev.lanthoor.spendly.utils.CurrencyUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
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

    private val editorService = IncomeEditorService(incomeRepository)

    // UI State for income list screen
    private val _uiState = MutableStateFlow<IncomeListUiState>(IncomeListUiState.Loading)
    val uiState: StateFlow<IncomeListUiState> = _uiState.asStateFlow()

    // Form state for add/edit screens
    private val _formState = MutableStateFlow(IncomeFormState())
    val formState: StateFlow<IncomeFormState> = _formState.asStateFlow()

    // Filter state
    private val _filters = MutableStateFlow(IncomeFilters())
    val filters: StateFlow<IncomeFilters> = _filters.asStateFlow()

    // Categories (all categories available for income)
    val incomeCategories: StateFlow<List<Category>> = categoryRepository.getAllCategories()
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
                    IncomeFilteringEngine.calculateTotalIncome(filters, incomeRepository)
                ) { incomes, total ->
                    IncomeListUiState.Success(
                        incomes = IncomeFilteringEngine.applyClientSideFilters(incomes, filters),
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
                                amount = CurrencyUtils.paiseToRupeeString(income.amount).replace(",", ""),
                                selectedCategory = category,
                                accountId = income.accountId,
                                source = income.source,
                                date = income.date,
                                description = income.description,
                                isRecurring = income.isRecurring,
                                linkedExpenseId = income.linkedExpenseId,
                                createdAt = income.createdAt,
                                isEditMode = true,
                                smsSourceId = income.smsSourceId,
                                smsBody = income.smsBody,
                                smsConfidence = income.smsConfidence,
                                smsTimestamp = income.smsTimestamp
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
                        amountError = IncomeFormValidator.validateAmount(amountStr)
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
                        descriptionError = IncomeFormValidator.validateDescription(descStr)
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
        val amountError = IncomeFormValidator.validateAmount(state.amount)
        val descError = IncomeFormValidator.validateDescription(state.description)

        _formState.update {
            it.copy(
                amountError = amountError,
                descriptionError = descError
            )
        }

        return amountError == null && descError == null
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
            val result = editorService.saveIncome(state)

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
        return editorService.deleteIncome(id)
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
     * Defaults to Salary category if available
     */
    fun resetForm() {
        val salaryCategory = Category.PREDEFINED.find { it.id == 101L }
        _formState.value = IncomeFormState(selectedCategory = salaryCategory)
    }
}
