package dev.lanthoor.spendly.ui.screens.income

import dev.lanthoor.spendly.domain.model.Income
import dev.lanthoor.spendly.domain.repository.IncomeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

object IncomeFilteringEngine {
    fun applyClientSideFilters(incomes: List<Income>, filters: IncomeFilters): List<Income> {
        var filtered = incomes

        if (filters.startDate != null && filters.endDate != null) {
            filtered = filtered.filter { it.date in filters.startDate..filters.endDate }
        }

        if (filters.sources.isNotEmpty()) {
            filtered = filtered.filter { it.source in filters.sources }
        }

        if (filters.recurringOnly) {
            filtered = filtered.filter { it.isRecurring }
        }

        return filtered
    }

    fun calculateTotalIncome(
        filters: IncomeFilters,
        incomeRepository: IncomeRepository
    ): Flow<Long> {
        return if (filters.startDate != null && filters.endDate != null) {
            incomeRepository.getTotalIncomeInRange(filters.startDate, filters.endDate)
        } else {
            incomeRepository.getAllIncome()
                .catch { emit(emptyList()) }
                .map { incomes -> incomes.sumOf { it.amount } }
        }
    }
}
