package dev.lanthoor.spendly.ui.screens.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.lanthoor.spendly.domain.model.Account
import dev.lanthoor.spendly.domain.repository.AccountRepository
import dev.lanthoor.spendly.domain.repository.ExpenseRepository
import dev.lanthoor.spendly.domain.repository.IncomeRepository
import dev.lanthoor.spendly.core.model.finance.AccountType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val expenseRepository: ExpenseRepository,
    private val incomeRepository: IncomeRepository
) : ViewModel() {

    private val _formState = MutableStateFlow(AccountFormState())
    val formState: StateFlow<AccountFormState> = _formState.asStateFlow()

    private val accountsFlow = accountRepository.getAllAccounts()
        .distinctUntilChanged()

    val accounts: StateFlow<List<Account>> = accountsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uiState: StateFlow<AccountUiState> = accountsFlow
        .map<List<Account>, AccountUiState> { list -> AccountUiState.Success(list) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AccountUiState.Loading)

    private val transactionCountFlows =
        mutableMapOf<Long, StateFlow<TransactionCount>>()

    fun getTransactionCount(accountId: Long): StateFlow<TransactionCount> {
        return transactionCountFlows.getOrPut(accountId) {
            combine(
                expenseRepository.getExpensesByAccount(accountId),
                incomeRepository.getIncomeByAccount(accountId)
            ) { expenses, income ->
                TransactionCount(
                    expenseCount = expenses.size,
                    incomeCount = income.size,
                    totalCount = expenses.size + income.size
                )
            }.distinctUntilChanged()
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TransactionCount())
        }
    }

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

            _formState.update { it.copy(isSubmitting = true) }

            val state = _formState.value

            if (!isNameUnique(state.name, if (state.isEditMode) state.id else null)) {
                _formState.value = state.copy(
                    isSubmitting = false,
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
                    sortOrder = if (state.isEditMode) 0 else 0,
                    createdAt = if (state.isEditMode) 0 else timestamp,
                    modifiedAt = timestamp
                )

                if (state.isEditMode) {
                    accountRepository.updateAccount(account)
                } else {
                    accountRepository.insertAccount(account)
                }

                _formState.update { it.copy(isSubmitting = false) }
                onSuccess()
            } catch (e: Exception) {
                _formState.update { it.copy(isSubmitting = false) }
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
}

sealed interface AccountUiState {
    data object Loading : AccountUiState
    data class Success(val accounts: List<Account>) : AccountUiState
    data class Error(val message: String) : AccountUiState
}

data class AccountFormState(
    val id: Long = 0,
    val name: String = "",
    val type: AccountType = AccountType.BANK,
    val icon: String = "bank",
    val color: Int = 0xFF00BFA5.toInt(),
    val isEditMode: Boolean = false,
    val isSubmitting: Boolean = false,
    val errors: Map<AccountFormField, String> = emptyMap()
)

enum class AccountFormField {
    NAME, TYPE, ICON, COLOR
}

data class TransactionCount(
    val expenseCount: Int = 0,
    val incomeCount: Int = 0,
    val totalCount: Int = 0
)
