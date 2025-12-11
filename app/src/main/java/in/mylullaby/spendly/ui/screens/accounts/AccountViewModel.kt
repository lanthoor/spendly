package `in`.mylullaby.spendly.ui.screens.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.mylullaby.spendly.domain.model.Account
import `in`.mylullaby.spendly.domain.repository.AccountRepository
import `in`.mylullaby.spendly.domain.repository.ExpenseRepository
import `in`.mylullaby.spendly.domain.repository.IncomeRepository
import `in`.mylullaby.spendly.utils.AccountType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for account management screens.
 * Handles CRUD operations, validation, and transaction counts.
 */
@HiltViewModel
class AccountViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val expenseRepository: ExpenseRepository,
    private val incomeRepository: IncomeRepository
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow<AccountUiState>(AccountUiState.Loading)
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    // Form State
    private val _formState = MutableStateFlow(AccountFormState())
    val formState: StateFlow<AccountFormState> = _formState.asStateFlow()

    // All accounts
    val accounts: StateFlow<List<Account>> = accountRepository.getAllAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadAccounts()
    }

    fun loadAccounts() {
        viewModelScope.launch {
            _uiState.value = AccountUiState.Loading
            try {
                accountRepository.getAllAccounts().collect { accountList ->
                    _uiState.value = AccountUiState.Success(accountList)
                }
            } catch (e: Exception) {
                _uiState.value = AccountUiState.Error(e.message ?: "Failed to load accounts")
            }
        }
    }

    // Form operations

    fun initializeFormForAdd() {
        _formState.value = AccountFormState()
    }

    fun initializeFormForEdit(account: Account) {
        _formState.value = AccountFormState(
            id = account.id,
            name = account.name,
            type = account.type,
            icon = account.icon,
            color = account.color,
            isEditMode = true
        )
    }

    fun updateFormField(field: AccountFormField, value: Any) {
        _formState.value = when (field) {
            AccountFormField.NAME -> _formState.value.copy(name = value as String)
            AccountFormField.TYPE -> _formState.value.copy(type = value as AccountType)
            AccountFormField.ICON -> _formState.value.copy(icon = value as String)
            AccountFormField.COLOR -> _formState.value.copy(color = value as Int)
        }
    }

    fun validateForm(): Boolean {
        val state = _formState.value
        val errors = mutableMapOf<AccountFormField, String>()

        // Validate name
        if (state.name.isBlank()) {
            errors[AccountFormField.NAME] = "Account name is required"
        } else if (state.name.length > 50) {
            errors[AccountFormField.NAME] = "Account name is too long (max 50 characters)"
        }

        _formState.value = state.copy(errors = errors)
        return errors.isEmpty()
    }

    suspend fun isNameUnique(name: String, excludeId: Long? = null): Boolean {
        return accountRepository.isAccountNameUnique(name, excludeId)
    }

    fun saveAccount(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            if (!validateForm()) {
                onError("Please fix the errors in the form")
                return@launch
            }

            val state = _formState.value

            // Check name uniqueness
            if (!isNameUnique(state.name, if (state.isEditMode) state.id else null)) {
                _formState.value = state.copy(
                    errors = state.errors + (AccountFormField.NAME to "Account name already exists")
                )
                onError("Account name already exists")
                return@launch
            }

            try {
                val timestamp = System.currentTimeMillis()
                val account = Account(
                    id = if (state.isEditMode) state.id else 0,
                    name = state.name.trim(),
                    type = state.type,
                    icon = state.icon,
                    color = state.color,
                    isCustom = true,
                    sortOrder = if (state.isEditMode) 0 else 0, // Will be set by repository
                    createdAt = if (state.isEditMode) 0 else timestamp,
                    modifiedAt = timestamp
                )

                if (state.isEditMode) {
                    accountRepository.updateAccount(account)
                } else {
                    accountRepository.insertAccount(account)
                }

                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to save account")
            }
        }
    }

    fun deleteAccount(
        accountId: Long,
        replacementAccountId: Long,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Prevent deletion of default account
                if (accountId == Account.DEFAULT_ACCOUNT_ID) {
                    onError("Cannot delete the default account")
                    return@launch
                }

                accountRepository.deleteAccount(accountId, replacementAccountId)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to delete account")
            }
        }
    }

    fun getTransactionCount(accountId: Long): StateFlow<TransactionCount> {
        return combine(
            expenseRepository.getExpensesByAccount(accountId),
            incomeRepository.getIncomeByAccount(accountId)
        ) { expenses, income ->
            TransactionCount(
                expenseCount = expenses.size,
                incomeCount = income.size,
                totalCount = expenses.size + income.size
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TransactionCount())
    }
}

/**
 * UI state for account list screen
 */
sealed class AccountUiState {
    data object Loading : AccountUiState()
    data class Success(val accounts: List<Account>) : AccountUiState()
    data class Error(val message: String) : AccountUiState()
}

/**
 * Form state for add/edit account
 */
data class AccountFormState(
    val id: Long = 0,
    val name: String = "",
    val type: AccountType = AccountType.BANK,
    val icon: String = "bank",
    val color: Int = 0xFF00BFA5.toInt(),
    val isEditMode: Boolean = false,
    val errors: Map<AccountFormField, String> = emptyMap()
)

/**
 * Form fields for validation
 */
enum class AccountFormField {
    NAME, TYPE, ICON, COLOR
}

/**
 * Transaction count for an account
 */
data class TransactionCount(
    val expenseCount: Int = 0,
    val incomeCount: Int = 0,
    val totalCount: Int = 0
)
