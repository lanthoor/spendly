package dev.lanthoor.spendly.ui.screens.expenses

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.lanthoor.spendly.domain.model.Account
import dev.lanthoor.spendly.domain.model.Category
import dev.lanthoor.spendly.domain.model.Receipt
import dev.lanthoor.spendly.domain.repository.AccountRepository
import dev.lanthoor.spendly.domain.repository.CategoryRepository
import dev.lanthoor.spendly.domain.repository.ExpenseRepository
import dev.lanthoor.spendly.domain.repository.ReceiptRepository
import dev.lanthoor.spendly.utils.BudgetNotificationService
import dev.lanthoor.spendly.utils.CurrencyUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val receiptRepository: ReceiptRepository,
    private val accountRepository: AccountRepository,
    private val budgetNotificationService: BudgetNotificationService
) : ViewModel() {

    companion object {
        private const val TAG = "ExpenseViewModel"
    }

    private val editorService = ExpenseEditorService(expenseRepository)
    private val receiptService = ExpenseReceiptService(receiptRepository)

    private val _uiState = MutableStateFlow<ExpenseListUiState>(ExpenseListUiState.Loading)
    val uiState: StateFlow<ExpenseListUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(ExpenseFormState())
    val formState: StateFlow<ExpenseFormState> = _formState.asStateFlow()

    private val _filters = MutableStateFlow(ExpenseFilters())
    val filters: StateFlow<ExpenseFilters> = _filters.asStateFlow()

    val categories: StateFlow<List<Category>> = categoryRepository.getAllCategories()
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val accounts: StateFlow<List<Account>> = accountRepository.getAllAccounts()
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private var loadExpensesJob: Job? = null
    private var loadExpenseByIdJob: Job? = null
    private var loadReceiptsJob: Job? = null

    init {
        loadExpenses()
    }

    override fun onCleared() {
        super.onCleared()
        loadExpensesJob?.cancel()
        loadExpenseByIdJob?.cancel()
        loadReceiptsJob?.cancel()
    }

    fun loadExpenses() {
        loadExpensesJob?.cancel()

        loadExpensesJob = viewModelScope.launch {
            _uiState.value = ExpenseListUiState.Loading

            try {
                val filters = _filters.value

                val expensesFlow = when {
                    filters.startDate != null && filters.endDate != null -> {
                        expenseRepository.getExpensesByDateRange(filters.startDate, filters.endDate)
                    }

                    filters.categoryIds.isNotEmpty() -> {
                        val categoryId = filters.categoryIds.first()
                        expenseRepository.getExpensesByCategory(categoryId)
                    }

                    filters.accountIds.isNotEmpty() -> {
                        val accountId = filters.accountIds.first()
                        expenseRepository.getExpensesByAccount(accountId)
                    }

                    else -> expenseRepository.getAllExpenses()
                }

                combine(
                    expensesFlow,
                    ExpenseFilteringEngine.calculateTotalSpent(filters, expenseRepository)
                ) { expenses, total ->
                    ExpenseListUiState.Success(
                        expenses = ExpenseFilteringEngine.applyClientSideFilters(expenses, filters),
                        filters = filters,
                        totalSpent = CurrencyUtils.formatPaise(total)
                    )
                }.catch { error ->
                    _uiState.value = ExpenseListUiState.Error(
                        message = error.message ?: "Failed to load expenses"
                    )
                }.collect { state ->
                    _uiState.value = state
                }
            } catch (e: Exception) {
                _uiState.value = ExpenseListUiState.Error(
                    message = e.message ?: "Failed to load expenses"
                )
            }
        }
    }

    fun loadExpenseById(id: Long) {
        loadExpenseByIdJob?.cancel()

        loadExpenseByIdJob = viewModelScope.launch {
            try {
                expenseRepository.getExpenseById(id).collectLatest { expense ->
                    if (expense != null) {
                        _formState.update {
                            ExpenseFormState(
                                id = expense.id,
                                amount = CurrencyUtils.paiseToRupeeString(expense.amount)
                                    .replace(",", ""),
                                categoryId = expense.categoryId,
                                date = expense.date,
                                description = expense.description,
                                accountId = expense.accountId,
                                createdAt = expense.createdAt,
                                isEditMode = true,
                                smsSourceId = expense.smsSourceId,
                                smsBody = expense.smsBody,
                                smsConfidence = expense.smsConfidence,
                                smsTimestamp = expense.smsTimestamp
                            )
                        }
                        loadReceiptsForExpense(id)
                    } else {
                        _formState.update {
                            it.copy(submitError = "Expense not found")
                        }
                    }
                }
            } catch (e: Exception) {
                _formState.update {
                    it.copy(submitError = e.message ?: "Failed to load expense")
                }
            }
        }
    }

    fun loadReceiptsForExpense(expenseId: Long) {
        loadReceiptsJob?.cancel()

        loadReceiptsJob = viewModelScope.launch {
            try {
                receiptRepository.getReceiptsByExpense(expenseId).collectLatest { receipts ->
                    _formState.update {
                        it.copy(receipts = receipts, receiptError = null)
                    }
                }
            } catch (e: Exception) {
                _formState.update {
                    it.copy(receiptError = "Failed to load receipts: ${e.message}")
                }
            }
        }
    }

    suspend fun addReceipt(
        context: Context,
        expenseId: Long,
        sourceUri: Uri
    ): Result<Receipt> = withContext(Dispatchers.IO) {
        try {
            val savedReceipt = receiptService.addReceipt(context, expenseId, sourceUri).getOrThrow()

            withContext(Dispatchers.Main) {
                _formState.update { currentState ->
                    currentState.copy(
                        receipts = currentState.receipts + savedReceipt,
                        receiptError = null
                    )
                }
            }

            Result.success(savedReceipt)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add receipt", e)
            withContext(Dispatchers.Main) {
                _formState.update {
                    it.copy(receiptError = e.message ?: "Failed to add receipt")
                }
            }
            Result.failure(e)
        }
    }

    suspend fun deleteReceipt(@Suppress("UNUSED_PARAMETER") context: Context, receipt: Receipt): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                receiptService.deleteReceipt(receipt).getOrThrow()

                withContext(Dispatchers.Main) {
                    _formState.update { currentState ->
                        currentState.copy(
                            receipts = currentState.receipts.filter { it.id != receipt.id },
                            receiptError = null
                        )
                    }
                }

                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete receipt", e)
                withContext(Dispatchers.Main) {
                    _formState.update {
                        it.copy(receiptError = "Failed to delete receipt: ${e.message}")
                    }
                }
                Result.failure(e)
            }
        }

    fun updateFormField(field: FormField, value: Any) {
        _formState.update { currentState ->
            when (field) {
                FormField.AMOUNT -> {
                    val amountStr = value as String
                    currentState.copy(
                        amount = amountStr,
                        amountError = ExpenseFormValidator.validateAmount(amountStr)
                    )
                }

                FormField.CATEGORY_ID -> currentState.copy(categoryId = value as Long?)
                FormField.DATE -> currentState.copy(date = value as Long)
                FormField.DESCRIPTION -> {
                    val descStr = value as String
                    currentState.copy(
                        description = descStr,
                        descriptionError = ExpenseFormValidator.validateDescription(descStr)
                    )
                }

                FormField.ACCOUNT_ID -> currentState.copy(accountId = value as Long)
            }
        }
    }

    fun validateForm(): Boolean {
        val state = _formState.value
        val amountError = ExpenseFormValidator.validateAmount(state.amount)
        val descError = ExpenseFormValidator.validateDescription(state.description)

        _formState.update {
            it.copy(
                amountError = amountError,
                descriptionError = descError
            )
        }

        return amountError == null && descError == null
    }

    suspend fun saveExpense(): Result<Long> {
        if (!validateForm()) {
            return Result.failure(Exception("Please fix validation errors"))
        }

        _formState.update { it.copy(isSubmitting = true, submitError = null) }

        return try {
            val state = _formState.value
            val result = editorService.saveExpense(state)

            viewModelScope.launch(Dispatchers.IO) {
                try {
                    budgetNotificationService.checkBudgetsAndNotify()
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to check budget notifications", e)
                }
            }

            _formState.update { it.copy(isSubmitting = false) }
            result
        } catch (e: Exception) {
            _formState.update {
                it.copy(
                    isSubmitting = false,
                    submitError = e.message ?: "Failed to save expense"
                )
            }
            Result.failure(e)
        }
    }

    suspend fun deleteExpense(id: Long): Result<Unit> {
        return editorService.deleteExpense(id)
    }

    fun applyFilters(filters: ExpenseFilters) {
        _filters.value = filters
        loadExpenses()
    }

    fun clearFilters() {
        _filters.value = ExpenseFilters()
        loadExpenses()
    }

    fun resetForm() {
        _formState.value = ExpenseFormState(categoryId = Category.OTHERS_CATEGORY_ID)
    }
}
