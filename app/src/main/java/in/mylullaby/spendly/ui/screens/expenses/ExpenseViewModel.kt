package `in`.mylullaby.spendly.ui.screens.expenses

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.mylullaby.spendly.domain.model.Category
import `in`.mylullaby.spendly.domain.model.Expense
import `in`.mylullaby.spendly.domain.model.Receipt
import `in`.mylullaby.spendly.domain.repository.CategoryRepository
import `in`.mylullaby.spendly.domain.repository.ExpenseRepository
import `in`.mylullaby.spendly.domain.repository.ReceiptRepository
import `in`.mylullaby.spendly.utils.CurrencyUtils
import `in`.mylullaby.spendly.utils.FileUtils
import `in`.mylullaby.spendly.utils.ImageCompressor
import `in`.mylullaby.spendly.utils.PaymentMethod
import java.io.File
import kotlinx.coroutines.Dispatchers
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
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * ViewModel for managing expense-related screens (list, add, edit).
 * Handles state management, validation, and repository interactions.
 */
@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val receiptRepository: ReceiptRepository
) : ViewModel() {

    // UI State for expense list screen
    private val _uiState = MutableStateFlow<ExpenseListUiState>(ExpenseListUiState.Loading)
    val uiState: StateFlow<ExpenseListUiState> = _uiState.asStateFlow()

    // Form state for add/edit screens
    private val _formState = MutableStateFlow(ExpenseFormState())
    val formState: StateFlow<ExpenseFormState> = _formState.asStateFlow()

    // Filter state
    private val _filters = MutableStateFlow(ExpenseFilters())
    val filters: StateFlow<ExpenseFilters> = _filters.asStateFlow()

    // Categories (loaded once and cached)
    val categories: StateFlow<List<Category>> = categoryRepository.getAllCategories()
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        loadExpenses()
    }

    /**
     * Loads expenses based on current filters
     */
    fun loadExpenses() {
        viewModelScope.launch {
            _uiState.value = ExpenseListUiState.Loading

            try {
                val filters = _filters.value

                // Determine which repository method to use based on active filters
                val expensesFlow = when {
                    // Date range filter
                    filters.startDate != null && filters.endDate != null -> {
                        expenseRepository.getExpensesByDateRange(filters.startDate, filters.endDate)
                    }
                    // Category filter (single category for now, can extend to multiple)
                    filters.categoryIds.isNotEmpty() -> {
                        val categoryId = filters.categoryIds.first()
                        expenseRepository.getExpensesByCategory(categoryId)
                    }
                    // Payment method filter (single method for now)
                    filters.paymentMethods.isNotEmpty() -> {
                        val method = filters.paymentMethods.first()
                        expenseRepository.getExpensesByPaymentMethod(method)
                    }
                    // No filters - get all expenses
                    else -> expenseRepository.getAllExpenses()
                }

                // Combine with total spent calculation
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

    /**
     * Apply client-side filters when multiple filter types are active
     * (repository methods handle single filter type)
     */
    private fun applyClientSideFilters(expenses: List<Expense>): List<Expense> {
        val filters = _filters.value
        var filtered = expenses

        // Apply date range if set
        if (filters.startDate != null && filters.endDate != null) {
            filtered = filtered.filter { it.date in filters.startDate..filters.endDate }
        }

        // Apply category filter if set
        if (filters.categoryIds.isNotEmpty()) {
            filtered = filtered.filter { expense ->
                expense.categoryId in filters.categoryIds ||
                (expense.categoryId == null && filters.includeUncategorized)
            }
        }

        // Apply payment method filter if set
        if (filters.paymentMethods.isNotEmpty()) {
            filtered = filtered.filter { it.paymentMethod in filters.paymentMethods }
        }

        return filtered
    }

    /**
     * Calculates total spent based on current filters
     */
    private fun calculateTotalSpent(): Flow<Long> {
        val filters = _filters.value
        return if (filters.startDate != null && filters.endDate != null) {
            expenseRepository.getTotalSpentInRange(filters.startDate, filters.endDate)
        } else {
            // Calculate from all expenses
            expenseRepository.getAllExpenses()
                .catch { emit(emptyList()) }
                .map { expenses ->
                    expenses.sumOf { it.amount }
                }
        }
    }

    /**
     * Loads a specific expense for editing
     */
    fun loadExpenseById(id: Long) {
        viewModelScope.launch {
            try {
                expenseRepository.getExpenseById(id).collect { expense ->
                    if (expense != null) {
                        _formState.update {
                            ExpenseFormState(
                                id = expense.id,
                                amount = expense.fromPaise().toString(),
                                categoryId = expense.categoryId,
                                date = expense.date,
                                description = expense.description,
                                paymentMethod = expense.paymentMethod,
                                createdAt = expense.createdAt,
                                isEditMode = true
                            )
                        }
                        // Load receipts for this expense
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

    /**
     * Loads receipts for an expense
     */
    fun loadReceiptsForExpense(expenseId: Long) {
        viewModelScope.launch {
            try {
                receiptRepository.getReceiptsByExpense(expenseId).collect { receipts ->
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

    /**
     * Adds a receipt to the current expense.
     * Handles file validation, copying, compression, and database insertion.
     *
     * @param context Application context
     * @param expenseId The expense ID (must be saved first)
     * @param sourceUri URI of the file to attach
     * @return Result with Receipt on success, or error
     */
    suspend fun addReceipt(
        context: Context,
        expenseId: Long,
        sourceUri: Uri
    ): Result<Receipt> = withContext(Dispatchers.IO) {
        try {
            // Validate expense ID
            if (expenseId == 0L) {
                return@withContext Result.failure(Exception("Please save the expense before adding receipts"))
            }

            // Get file extension and validate type
            val extension = FileUtils.getFileExtension(sourceUri, context)
            if (!FileUtils.isSupportedFileType(extension)) {
                return@withContext Result.failure(Exception("Unsupported file type. Please select JPG, PNG, WebP, or PDF"))
            }

            // Get file size and validate
            val fileSize = FileUtils.getFileSizeFromUri(sourceUri, context)
            if (!FileUtils.validateFileSize(fileSize)) {
                return@withContext Result.failure(Exception("File too large. Maximum ${FileUtils.formatFileSize(FileUtils.MAX_FILE_SIZE_BYTES)} allowed"))
            }

            // Check storage space
            if (!FileUtils.hasEnoughStorage(context, fileSize + (1024 * 1024))) { // +1MB buffer
                return@withContext Result.failure(Exception("Not enough storage space"))
            }

            // Generate unique filename
            val timestamp = System.currentTimeMillis()
            val fileName = FileUtils.generateReceiptFileName(expenseId, timestamp, extension)
            val receiptsDir = FileUtils.getReceiptsDirectory(context)
            val destFile = File(receiptsDir, fileName)

            // Compress/copy file
            val compressionResult = ImageCompressor.compressImage(
                context = context,
                sourceUri = sourceUri,
                destFile = destFile,
                fileExtension = extension
            )

            if (!compressionResult.success) {
                return@withContext Result.failure(Exception(compressionResult.error ?: "Failed to process file"))
            }

            // Create receipt entity
            val receipt = Receipt(
                expenseId = expenseId,
                filePath = "receipts/$fileName",
                fileType = extension.uppercase(),
                fileSizeBytes = compressionResult.fileSizeBytes,
                compressed = compressionResult.wasCompressed
            )

            // Insert into database
            val receiptId = receiptRepository.insertReceipt(receipt)
            val savedReceipt = receipt.copy(id = receiptId)

            // Update form state
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
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                _formState.update {
                    it.copy(receiptError = e.message ?: "Failed to add receipt")
                }
            }
            Result.failure(e)
        }
    }

    /**
     * Deletes a receipt
     */
    suspend fun deleteReceipt(context: Context, receipt: Receipt): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            receiptRepository.deleteReceipt(receipt)

            // Update form state
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
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                _formState.update {
                    it.copy(receiptError = "Failed to delete receipt: ${e.message}")
                }
            }
            Result.failure(e)
        }
    }

    /**
     * Updates a form field
     */
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
                FormField.PAYMENT_METHOD -> currentState.copy(paymentMethod = value as PaymentMethod)
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
     * Saves the expense (create or update)
     */
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
                paymentMethod = state.paymentMethod,
                createdAt = state.createdAt ?: currentTime,
                modifiedAt = currentTime
            )

            val result = if (state.isEditMode) {
                expenseRepository.updateExpense(expense)
                Result.success(expense.id)
            } else {
                val id = expenseRepository.insertExpense(expense)
                Result.success(id)
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

    /**
     * Deletes an expense
     */
    suspend fun deleteExpense(id: Long): Result<Unit> {
        return try {
            val expense = Expense(
                id = id,
                amount = 0,
                categoryId = null,
                date = 0,
                description = "",
                paymentMethod = PaymentMethod.CASH,
                createdAt = 0,
                modifiedAt = 0
            )
            expenseRepository.deleteExpense(expense)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Applies filters and reloads expenses
     */
    fun applyFilters(filters: ExpenseFilters) {
        _filters.value = filters
        loadExpenses()
    }

    /**
     * Clears all filters
     */
    fun clearFilters() {
        _filters.value = ExpenseFilters()
        loadExpenses()
    }

    /**
     * Resets form state (for new expense entry)
     */
    fun resetForm() {
        _formState.value = ExpenseFormState()
    }
}

/**
 * UI state for expense list screen
 */
sealed interface ExpenseListUiState {
    data object Loading : ExpenseListUiState
    data class Success(
        val expenses: List<Expense>,
        val filters: ExpenseFilters,
        val totalSpent: String
    ) : ExpenseListUiState
    data class Error(val message: String) : ExpenseListUiState
}

/**
 * Form state for add/edit expense screens
 */
data class ExpenseFormState(
    val id: Long = 0,
    val amount: String = "",
    val amountError: String? = null,
    val categoryId: Long? = null,
    val date: Long = System.currentTimeMillis(),
    val description: String = "",
    val descriptionError: String? = null,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val createdAt: Long? = null,
    val isEditMode: Boolean = false,
    val isSubmitting: Boolean = false,
    val submitError: String? = null,
    val receipts: List<Receipt> = emptyList(),
    val receiptError: String? = null
)

/**
 * Filter state for expense list
 */
data class ExpenseFilters(
    val startDate: Long? = null,
    val endDate: Long? = null,
    val categoryIds: Set<Long> = emptySet(),
    val paymentMethods: Set<PaymentMethod> = emptySet(),
    val includeUncategorized: Boolean = true
)

/**
 * Form fields enum for type-safe updates
 */
enum class FormField {
    AMOUNT,
    CATEGORY_ID,
    DATE,
    DESCRIPTION,
    PAYMENT_METHOD
}
