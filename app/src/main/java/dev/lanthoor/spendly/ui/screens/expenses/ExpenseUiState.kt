package dev.lanthoor.spendly.ui.screens.expenses

import dev.lanthoor.spendly.domain.model.Expense

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
 * Filter state for expense list
 */
data class ExpenseFilters(
    val startDate: Long? = null,
    val endDate: Long? = null,
    val categoryIds: Set<Long> = emptySet(),
    val accountIds: Set<Long> = emptySet(),
    val includeOthers: Boolean = true // Include expenses with "Others" category
)
