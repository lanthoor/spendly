package dev.lanthoor.spendly.ui.screens.income

import dev.lanthoor.spendly.core.model.finance.IncomeSource
import dev.lanthoor.spendly.domain.model.Account
import dev.lanthoor.spendly.domain.model.Category
import dev.lanthoor.spendly.domain.model.Income

sealed interface IncomeListUiState {
    data object Loading : IncomeListUiState
    data class Success(
        val incomes: List<Income>,
        val filters: IncomeFilters,
        val totalIncome: String
    ) : IncomeListUiState

    data class Error(val message: String) : IncomeListUiState
}

data class IncomeFormState(
    val id: Long = 0,
    val amount: String = "",
    val amountError: String? = null,
    val selectedCategory: Category? = null,
    val accountId: Long = Account.DEFAULT_ACCOUNT_ID,
    val source: IncomeSource = IncomeSource.SALARY,
    val date: Long = System.currentTimeMillis(),
    val description: String = "",
    val descriptionError: String? = null,
    val isRecurring: Boolean = false,
    val linkedExpenseId: Long? = null,
    val createdAt: Long? = null,
    val isEditMode: Boolean = false,
    val isSubmitting: Boolean = false,
    val submitError: String? = null,
    val smsSourceId: Long? = null,
    val smsBody: String? = null,
    val smsConfidence: Float? = null,
    val smsTimestamp: Long? = null
)

data class IncomeFilters(
    val startDate: Long? = null,
    val endDate: Long? = null,
    val sources: Set<IncomeSource> = emptySet(),
    val recurringOnly: Boolean = false
)

enum class IncomeFormField {
    AMOUNT,
    CATEGORY,
    ACCOUNT_ID,
    SOURCE,
    DATE,
    DESCRIPTION,
    IS_RECURRING,
    LINKED_EXPENSE_ID
}
