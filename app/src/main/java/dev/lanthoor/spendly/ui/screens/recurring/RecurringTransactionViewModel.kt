package dev.lanthoor.spendly.ui.screens.recurring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.lanthoor.spendly.domain.model.Account
import dev.lanthoor.spendly.domain.model.Category
import dev.lanthoor.spendly.domain.model.RecurringTransaction
import dev.lanthoor.spendly.domain.repository.AccountRepository
import dev.lanthoor.spendly.domain.repository.CategoryRepository
import dev.lanthoor.spendly.domain.repository.RecurringTransactionRepository
import dev.lanthoor.spendly.utils.CurrencyUtils
import dev.lanthoor.spendly.utils.RecurringFrequency
import dev.lanthoor.spendly.utils.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing recurring transactions.
 * Handles state management, validation, and repository interactions.
 */
@HiltViewModel
class RecurringTransactionViewModel @Inject constructor(
    private val recurringTransactionRepository: RecurringTransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val accountRepository: AccountRepository
) : ViewModel() {

    // UI State for list screen
    private val _uiState =
        MutableStateFlow<RecurringTransactionListUiState>(RecurringTransactionListUiState.Loading)
    val uiState: StateFlow<RecurringTransactionListUiState> = _uiState.asStateFlow()

    // Form state for add/edit screens
    private val _formState = MutableStateFlow(RecurringTransactionFormState())
    val formState: StateFlow<RecurringTransactionFormState> = _formState.asStateFlow()

    // All categories (available for both expense and income)
    val allCategories: StateFlow<List<Category>> = categoryRepository.getAllCategories()
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
        loadRecurringTransactions()
    }

    /**
     * Loads all recurring transactions from repository.
     */
    fun loadRecurringTransactions() {
        viewModelScope.launch {
            _uiState.value = RecurringTransactionListUiState.Loading
            try {
                recurringTransactionRepository.getAllRecurringTransactions()
                    .catch { e ->
                        _uiState.value = RecurringTransactionListUiState.Error(
                            e.message ?: "Failed to load recurring transactions"
                        )
                    }
                    .collect { transactions ->
                        _uiState.value = RecurringTransactionListUiState.Success(transactions)
                    }
            } catch (e: Exception) {
                _uiState.value = RecurringTransactionListUiState.Error(
                    e.message ?: "Failed to load recurring transactions"
                )
            }
        }
    }

    /**
     * Loads a specific recurring transaction for editing.
     */
    fun loadRecurringTransactionById(id: Long) {
        viewModelScope.launch {
            try {
                recurringTransactionRepository.getRecurringTransactionById(id)
                    .collect { transaction ->
                        transaction?.let {
                            _formState.value = RecurringTransactionFormState(
                                id = it.id,
                                transactionType = TransactionType.fromStringOrDefault(it.transactionType),
                                amount = CurrencyUtils.paiseToRupeeString(it.amount),
                                categoryId = it.categoryId,
                                accountId = it.accountId,
                                description = it.description,
                                frequency = RecurringFrequency.fromStringOrDefault(it.frequency),
                                nextDate = it.nextDate,
                                createdAt = it.createdAt,
                                isEditMode = true
                            )
                        }
                    }
            } catch (e: Exception) {
                _formState.update {
                    it.copy(submitError = "Failed to load recurring transaction")
                }
            }
        }
    }

    /**
     * Updates a form field.
     */
    fun updateFormField(field: RecurringTransactionFormField, value: Any) {
        _formState.update { state ->
            when (field) {
                RecurringTransactionFormField.TRANSACTION_TYPE -> {
                    val type = value as TransactionType
                    state.copy(
                        transactionType = type,
                        categoryId = null, // Reset category when type changes
                        amountError = null,
                        descriptionError = null
                    )
                }

                RecurringTransactionFormField.AMOUNT -> state.copy(
                    amount = value as String,
                    amountError = null
                )

                RecurringTransactionFormField.CATEGORY -> state.copy(
                    categoryId = value as Long?,
                    amountError = null,
                    descriptionError = null
                )

                RecurringTransactionFormField.ACCOUNT -> state.copy(
                    accountId = value as Long,
                    amountError = null,
                    descriptionError = null
                )

                RecurringTransactionFormField.DESCRIPTION -> state.copy(
                    description = value as String,
                    descriptionError = null
                )

                RecurringTransactionFormField.FREQUENCY -> state.copy(
                    frequency = value as RecurringFrequency,
                    amountError = null,
                    descriptionError = null
                )

                RecurringTransactionFormField.NEXT_DATE -> state.copy(
                    nextDate = value as Long,
                    amountError = null,
                    descriptionError = null
                )
            }
        }
    }

    /**
     * Validates the form.
     */
    private fun validateForm(): Boolean {
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
     * Validates amount field.
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
     * Validates description field.
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
     * Saves the recurring transaction (create or update).
     */
    suspend fun saveRecurringTransaction(): Result<Long> {
        if (!validateForm()) {
            return Result.failure(Exception("Please fix validation errors"))
        }

        _formState.update { it.copy(isSubmitting = true, submitError = null) }

        return try {
            val state = _formState.value
            val amountInPaise = CurrencyUtils.parseRupeesToPaise(state.amount)
            val currentTime = System.currentTimeMillis()

            val recurringTransaction = RecurringTransaction(
                id = state.id,
                transactionType = state.transactionType.name,
                amount = amountInPaise,
                categoryId = state.categoryId ?: 0L, // Provide default if null
                accountId = state.accountId,
                description = state.description.trim(),
                frequency = state.frequency.name,
                nextDate = state.nextDate,
                lastProcessed = null, // Will be set by processor
                createdAt = state.createdAt ?: currentTime,
                modifiedAt = currentTime
            )

            val result = if (state.isEditMode) {
                recurringTransactionRepository.updateRecurringTransaction(recurringTransaction)
                Result.success(recurringTransaction.id)
            } else {
                val id =
                    recurringTransactionRepository.insertRecurringTransaction(recurringTransaction)
                Result.success(id)
            }

            _formState.update { it.copy(isSubmitting = false) }
            result
        } catch (e: Exception) {
            _formState.update {
                it.copy(
                    isSubmitting = false,
                    submitError = e.message ?: "Failed to save recurring transaction"
                )
            }
            Result.failure(e)
        }
    }

    /**
     * Deletes a recurring transaction.
     */
    suspend fun deleteRecurringTransaction(id: Long): Result<Unit> {
        return try {
            val transaction = RecurringTransaction(
                id = id,
                transactionType = TransactionType.EXPENSE.name,
                amount = 0L,
                categoryId = 0L, // Provide default value
                accountId = Account.DEFAULT_ACCOUNT_ID,
                description = "",
                frequency = RecurringFrequency.MONTHLY.name,
                nextDate = System.currentTimeMillis(),
                lastProcessed = null,
                createdAt = System.currentTimeMillis(),
                modifiedAt = System.currentTimeMillis()
            )
            recurringTransactionRepository.deleteRecurringTransaction(transaction)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Resets the form state.
     */
    fun resetForm() {
        _formState.value = RecurringTransactionFormState()
    }
}

/**
 * UI state for recurring transaction list screen.
 */
sealed interface RecurringTransactionListUiState {
    data object Loading : RecurringTransactionListUiState
    data class Success(val transactions: List<RecurringTransaction>) :
        RecurringTransactionListUiState

    data class Error(val message: String) : RecurringTransactionListUiState
}

/**
 * Form state for add/edit recurring transaction screens.
 */
data class RecurringTransactionFormState(
    val id: Long = 0,
    val transactionType: TransactionType = TransactionType.EXPENSE,
    val amount: String = "",
    val categoryId: Long? = null,
    val accountId: Long = Account.DEFAULT_ACCOUNT_ID,
    val description: String = "",
    val frequency: RecurringFrequency = RecurringFrequency.MONTHLY,
    val nextDate: Long = System.currentTimeMillis(),
    val createdAt: Long? = null,
    // Validation errors
    val amountError: String? = null,
    val descriptionError: String? = null,
    // Submission state
    val isEditMode: Boolean = false,
    val isSubmitting: Boolean = false,
    val submitError: String? = null
)

/**
 * Form fields enum for type-safe updates.
 */
enum class RecurringTransactionFormField {
    TRANSACTION_TYPE,
    AMOUNT,
    CATEGORY,
    ACCOUNT,
    DESCRIPTION,
    FREQUENCY,
    NEXT_DATE
}
