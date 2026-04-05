package dev.lanthoor.spendly.ui.screens.budgets.api

import dev.lanthoor.spendly.core.model.finance.BudgetWithProgress

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
