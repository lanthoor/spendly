package dev.lanthoor.spendly.ui.screens.expenses

import dev.lanthoor.spendly.domain.model.Expense
import dev.lanthoor.spendly.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

object ExpenseFilteringEngine {
    fun applyClientSideFilters(expenses: List<Expense>, filters: ExpenseFilters): List<Expense> {
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

    fun calculateTotalSpent(
        filters: ExpenseFilters,
        expenseRepository: ExpenseRepository
    ): Flow<Long> {
        return if (filters.startDate != null && filters.endDate != null) {
            expenseRepository.getTotalSpentInRange(filters.startDate, filters.endDate)
        } else {
            expenseRepository.getAllExpenses()
                .catch { emit(emptyList()) }
                .map { expenses -> expenses.sumOf { it.amount } }
        }
    }
}
