package `in`.co.spendly.ui.screens.expenses

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.co.spendly.domain.model.Account
import `in`.co.spendly.domain.model.Category
import `in`.co.spendly.domain.model.Expense
import `in`.co.spendly.domain.model.Receipt
import `in`.co.spendly.domain.repository.AccountRepository
import `in`.co.spendly.domain.repository.CategoryRepository
import `in`.co.spendly.domain.repository.ExpenseRepository
import `in`.co.spendly.domain.repository.ReceiptRepository
import `in`.co.spendly.utils.BudgetNotificationService
import `in`.co.spendly.utils.CurrencyUtils
import `in`.co.spendly.utils.FileTypeValidator
import `in`.co.spendly.utils.FileUtils
import `in`.co.spendly.utils.ImageCompressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
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
                    calculateTotalSpent()
                ) { expenses, total ->
                    ExpenseListUiState.Success(
                        expenses = applyClientSideFilters(expenses),
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

    private fun applyClientSideFilters(expenses: List<Expense>): List<Expense> {
        val filters = _filters.value
        var filtered = expenses

        if (filters.startDate != null && filters.endDate != null) {
            filtered = filtered.filter { it.date in filters.startDate..filters.endDate }
        }

        if (filters.categoryIds.isNotEmpty()) {
            filtered = filtered.filter { expense ->
                expense.categoryId in filters.categoryIds ||
                        (expense.categoryId == null && filters.includeOthers)
            }
        }

        if (filters.accountIds.isNotEmpty()) {
            filtered = filtered.filter { it.accountId in filters.accountIds }
        }

        return filtered
    }

    private fun calculateTotalSpent(): Flow<Long> {
        val filters = _filters.value
        return if (filters.startDate != null && filters.endDate != null) {
            expenseRepository.getTotalSpentInRange(filters.startDate, filters.endDate)
        } else {
            expenseRepository.getAllExpenses()
                .catch { emit(emptyList()) }
                .map { expenses ->
                    expenses.sumOf { it.amount }
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
        var tempFile: File? = null
        try {
            if (expenseId == 0L) {
                return@withContext Result.failure(Exception("Please save the expense before adding receipts"))
            }

            val extension = FileUtils.getFileExtension(sourceUri, context)

            tempFile = FileUtils.copyUriToTempFile(context, sourceUri, extension)
            if (tempFile == null) {
                return@withContext Result.failure(Exception("Failed to read file"))
            }

            val validationResult = FileUtils.validateReceiptFile(tempFile)
            if (validationResult != FileTypeValidator.ValidationResult.Valid) {
                return@withContext Result.failure(Exception(validationResult.getErrorMessage()))
            }

            val fileSize = tempFile.length()
            if (!FileUtils.hasEnoughStorage(context, fileSize + (1024 * 1024))) {
                return@withContext Result.failure(Exception("Not enough storage space"))
            }

            val timestamp = System.currentTimeMillis()
            val fileName = FileUtils.generateReceiptFileName(expenseId, timestamp, extension)
            val receiptsDir = FileUtils.getReceiptsDirectory(context)
            val destFile = File(receiptsDir, fileName)

            val compressionResult = ImageCompressor.compressImage(
                context = context,
                sourceUri = sourceUri,
                destFile = destFile,
                fileExtension = extension
            )

            if (!compressionResult.success) {
                return@withContext Result.failure(
                    Exception(
                        compressionResult.error ?: "Failed to process file"
                    )
                )
            }

            val receipt = Receipt(
                expenseId = expenseId,
                filePath = "receipts/$fileName",
                fileType = extension.uppercase(),
                fileSizeBytes = compressionResult.fileSizeBytes,
                compressed = compressionResult.wasCompressed
            )

            val receiptId = receiptRepository.insertReceipt(receipt)
            val savedReceipt = receipt.copy(id = receiptId)

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
        } finally {
            tempFile?.delete()
        }
    }

    suspend fun deleteReceipt(context: Context, receipt: Receipt): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                receiptRepository.deleteReceipt(receipt)

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
                        amountError = validateAmount(amountStr)
                    )
                }

                FormField.CATEGORY_ID -> currentState.copy(categoryId = value as Long?)
                FormField.DATE -> currentState.copy(date = value as Long)
                FormField.DESCRIPTION -> {
                    val descStr = value as String
                    currentState.copy(
                        description = descStr,
                        descriptionError = validateDescription(descStr)
                    )
                }

                FormField.ACCOUNT_ID -> currentState.copy(accountId = value as Long)
            }
        }
    }

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

    private fun validateAmount(amount: String): String? {
        val cleanAmount = amount.replace(",", "")
        return when {
            cleanAmount.isBlank() -> "Amount is required"
            cleanAmount.toDoubleOrNull() == null -> "Invalid amount format"
            cleanAmount.toDouble() <= 0 -> "Amount must be greater than 0"
            else -> null
        }
    }

    private fun validateDescription(description: String): String? {
        return when {
            description.isBlank() -> "Description is required"
            description.length < 3 -> "Description must be at least 3 characters"
            description.length > 200 -> "Description must not exceed 200 characters"
            else -> null
        }
    }

    suspend fun saveExpense(): Result<Long> {
        if (!validateForm()) {
            return Result.failure(Exception("Please fix validation errors"))
        }

        _formState.update { it.copy(isSubmitting = true, submitError = null) }

        return try {
            val state = _formState.value
            val amountInPaise = CurrencyUtils.parseRupeesToPaise(state.amount)
            val currentTime = System.currentTimeMillis()

            val expense = Expense(
                id = state.id,
                amount = amountInPaise,
                categoryId = state.categoryId,
                date = state.date,
                description = state.description.trim(),
                accountId = state.accountId,
                createdAt = state.createdAt ?: currentTime,
                modifiedAt = currentTime,
                smsSourceId = state.smsSourceId,
                smsBody = state.smsBody,
                smsConfidence = state.smsConfidence,
                smsTimestamp = state.smsTimestamp
            )

            val result = if (state.isEditMode) {
                expenseRepository.updateExpense(expense)
                Result.success(expense.id)
            } else {
                val id = expenseRepository.insertExpense(expense)
                Result.success(id)
            }

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
        return try {
            val expense = Expense(
                id = id,
                amount = 0,
                categoryId = null,
                date = 0,
                description = "",
                accountId = Account.DEFAULT_ACCOUNT_ID,
                createdAt = 0,
                modifiedAt = 0
            )
            expenseRepository.deleteExpense(expense)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
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
