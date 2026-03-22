package `in`.co.spendly.ui.screens.budgets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.co.spendly.domain.model.Budget
import `in`.co.spendly.domain.model.Category
import `in`.co.spendly.domain.repository.BudgetRepository
import `in`.co.spendly.domain.repository.CategoryRepository
import `in`.co.spendly.domain.repository.ExpenseRepository
import `in`.co.spendly.utils.CurrencyUtils
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
    private val expenseRepository: ExpenseRepository
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
                    // Get latest budget per category (including overall)
                    // Group by categoryId (null for overall) and take the one with latest month/year
                    val latestBudgets = allBudgets
                        .groupBy { it.categoryId }
                        .mapValues { (_, budgets) ->
                            budgets.maxByOrNull { budget ->
                                budget.year * 12 + budget.month // Sort by year+month
                            }
                        }
                        .values
                        .filterNotNull()

                    // Filter to only active budgets (set on or before current month)
                    val activeBudgets = latestBudgets.filter { budget ->
                        val budgetMonthIndex = budget.year * 12 + budget.month
                        val currentMonthIndex = currentYear * 12 + currentMonth
                        budgetMonthIndex <= currentMonthIndex
                    }

                    // Calculate current month's expenses
                    val monthStart = Calendar.getInstance().apply {
                        set(Calendar.YEAR, currentYear)
                        set(Calendar.MONTH, currentMonth - 1)
                        set(Calendar.DAY_OF_MONTH, 1)
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis

                    val monthEnd = Calendar.getInstance().apply {
                        set(Calendar.YEAR, currentYear)
                        set(Calendar.MONTH, currentMonth - 1)
                        set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                        set(Calendar.HOUR_OF_DAY, 23)
                        set(Calendar.MINUTE, 59)
                        set(Calendar.SECOND, 59)
                        set(Calendar.MILLISECOND, 999)
                    }.timeInMillis

                    val currentMonthExpenses = expenses.filter { it.date in monthStart..monthEnd }

                    // Calculate progress for each active budget
                    val budgetsWithProgress = activeBudgets.map { budget ->
                        val spent = calculateSpentForBudget(budget, currentMonthExpenses)
                        val category = budget.categoryId?.let { id ->
                            categories.find { it.id == id }
                        }
                        val progress = budget.calculateProgress(spent)

                        BudgetWithProgress(
                            budget = budget,
                            category = category,
                            currentSpent = spent,
                            progress = progress,
                            shouldNotify75 = budget.shouldNotify75(spent),
                            shouldNotify100 = budget.shouldNotify100(spent)
                        )
                    }.sortedByDescending { it.progress } // Sort by progress descending

                    val hasOverallBudget = activeBudgets.any { it.isOverallBudget() }

                    BudgetListUiState.Success(
                        budgets = budgetsWithProgress,
                        selectedMonth = currentMonth,
                        selectedYear = currentYear,
                        hasOverallBudget = hasOverallBudget
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
     * Calculate spent amount for a budget (already filtered to current month expenses).
     */
    private fun calculateSpentForBudget(
        budget: Budget,
        monthExpenses: List<`in`.co.spendly.domain.model.Expense>
    ): Long {
        return if (budget.categoryId != null) {
            monthExpenses
                .filter { it.categoryId == budget.categoryId }
                .sumOf { it.amount }
        } else {
            // Overall budget includes all expenses
            monthExpenses.sumOf { it.amount }
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
 * UI state for budget list screen.
 */
sealed interface BudgetListUiState {
    data object Loading : BudgetListUiState
    data class Success(
        val budgets: List<BudgetWithProgress>,
        val selectedMonth: Int,
        val selectedYear: Int,
        val hasOverallBudget: Boolean
    ) : BudgetListUiState

    data class Error(val message: String) : BudgetListUiState
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

/**
 * Budget with calculated progress information.
 */
data class BudgetWithProgress(
    val budget: Budget,
    val category: Category?, // null for overall budget
    val currentSpent: Long, // in paise
    val progress: Float, // 0.0 to 100.0+
    val shouldNotify75: Boolean,
    val shouldNotify100: Boolean
)
