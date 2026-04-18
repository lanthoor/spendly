package dev.lanthoor.spendly.ui.screens.budgets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.lanthoor.spendly.domain.usecase.budgets.BudgetListStateInput
import dev.lanthoor.spendly.domain.usecase.budgets.BuildBudgetListStateUseCase
import dev.lanthoor.spendly.ui.screens.budgets.api.BudgetListUiState
import dev.lanthoor.spendly.domain.model.Budget
import dev.lanthoor.spendly.domain.model.Category
import dev.lanthoor.spendly.domain.repository.BudgetRepository
import dev.lanthoor.spendly.domain.repository.CategoryRepository
import dev.lanthoor.spendly.domain.repository.ExpenseRepository
import dev.lanthoor.spendly.utils.CurrencyUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

/**
 * ViewModel for managing budget-related screens.
 * Handles budget CRUD operations, progress calculation, and state management.
 */
@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository,
    private val expenseRepository: ExpenseRepository,
    private val buildBudgetListStateUseCase: BuildBudgetListStateUseCase
) : ViewModel() {

    // UI State for budget list screen
    private val _uiState = MutableStateFlow<BudgetListUiState>(BudgetListUiState.Loading)
    val uiState: StateFlow<BudgetListUiState> = _uiState.asStateFlow()

    // Form state for add/edit screens
    private val _formState = MutableStateFlow(BudgetFormState())
    val formState: StateFlow<BudgetFormState> = _formState.asStateFlow()

    // Categories (loaded once and cached, all categories available for budgeting)
    val expenseCategories: StateFlow<List<Category>> = categoryRepository.getAllCategories()
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        loadBudgets()
    }

    /**
     * Load budgets for current month with progress calculation.
     * Uses latest budget per category.
     */
    fun loadBudgets() {
        viewModelScope.launch {
            _uiState.value = BudgetListUiState.Loading

            try {
                // Get current month/year
                val calendar = Calendar.getInstance()
                val currentMonth = calendar.get(Calendar.MONTH) + 1
                val currentYear = calendar.get(Calendar.YEAR)

                combine(
                    budgetRepository.getAllBudgets(),
                    expenseRepository.getAllExpenses(),
                    categoryRepository.getAllCategories()
                ) { allBudgets, expenses, categories ->
                    val result = buildBudgetListStateUseCase.execute(
                        BudgetListStateInput(
                            allBudgets = allBudgets,
                            expenses = expenses,
                            categories = categories,
                            currentMonth = currentMonth,
                            currentYear = currentYear
                        )
                    )

                    BudgetListUiState.Success(
                        budgets = result.budgets,
                        selectedMonth = result.selectedMonth,
                        selectedYear = result.selectedYear,
                        hasOverallBudget = result.hasOverallBudget
                    )
                }.collect { state ->
                    _uiState.value = state
                }
            } catch (e: Exception) {
                _uiState.value = BudgetListUiState.Error(e.message ?: "Failed to load budgets")
            }
        }
    }

    /**
     * Load a budget by ID for editing.
     */
    fun loadBudgetById(budgetId: Long) {
        viewModelScope.launch {
            try {
                val budget = budgetRepository.getBudgetById(budgetId).first()
                if (budget != null) {
                    _formState.value = BudgetFormState(
                        id = budget.id,
                        categoryId = budget.categoryId,
                        amount = CurrencyUtils.paiseToRupeeString(budget.amount),
                        isEditMode = true
                    )
                }
            } catch (e: Exception) {
                _formState.value = _formState.value.copy(
                    submitError = "Failed to load budget: ${e.message}"
                )
            }
        }
    }

    /**
     * Update form field values.
     */
    fun updateAmount(value: String) {
        _formState.value = _formState.value.copy(
            amount = value,
            amountError = null
        )
    }

    fun updateCategory(categoryId: Long?) {
        _formState.value = _formState.value.copy(categoryId = categoryId)
    }

    /**
     * Save budget (create or update).
     * Always uses current month/year.
     */
    fun saveBudget(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val formState = _formState.value

            // Validate amount
            val amountPaise = try {
                val rupees = formState.amount.toDouble()
                (rupees * 100).toLong() // Convert rupees to paise
            } catch (e: Exception) {
                _formState.value = formState.copy(amountError = "Invalid amount")
                return@launch
            }

            if (amountPaise <= 0) {
                _formState.value = formState.copy(amountError = "Amount must be greater than 0")
                return@launch
            }

            _formState.value = formState.copy(isSubmitting = true, submitError = null)

            try {
                // Get current month/year
                val calendar = Calendar.getInstance()
                val currentMonth = calendar.get(Calendar.MONTH) + 1
                val currentYear = calendar.get(Calendar.YEAR)

                val budget = Budget(
                    id = formState.id,
                    categoryId = formState.categoryId,
                    amount = amountPaise,
                    month = currentMonth,
                    year = currentYear,
                    notification75Sent = false,
                    notification100Sent = false
                )

                if (formState.isEditMode) {
                    budgetRepository.updateBudget(budget)
                } else {
                    budgetRepository.insertBudget(budget)
                }

                resetForm()
                loadBudgets()
                onSuccess()
            } catch (e: Exception) {
                _formState.value = formState.copy(
                    isSubmitting = false,
                    submitError = "Failed to save budget: ${e.message}"
                )
            }
        }
    }

    /**
     * Delete a budget.
     */
    fun deleteBudget(budgetId: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val budget = budgetRepository.getBudgetById(budgetId).first()
                if (budget != null) {
                    budgetRepository.deleteBudget(budget)
                    loadBudgets()
                    onSuccess()
                }
            } catch (e: Exception) {
                _formState.value = _formState.value.copy(
                    submitError = "Failed to delete budget: ${e.message}"
                )
            }
        }
    }

    /**
     * Reset form to initial state.
     */
    fun resetForm() {
        _formState.value = BudgetFormState()
    }
}

/**
 * Form state for add/edit budget screens.
 * Month/year are automatically set to current when saving.
 */
data class BudgetFormState(
    val id: Long = 0,
    val categoryId: Long? = null, // null = overall budget
    val amount: String = "",
    val amountError: String? = null,
    val isEditMode: Boolean = false,
    val isSubmitting: Boolean = false,
    val submitError: String? = null
)
