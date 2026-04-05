package dev.lanthoor.spendly.ui.screens.budgets.usecase

import dev.lanthoor.spendly.core.model.finance.BudgetWithProgress
import dev.lanthoor.spendly.domain.model.Budget
import dev.lanthoor.spendly.domain.model.Category
import dev.lanthoor.spendly.domain.model.Expense
import dev.lanthoor.spendly.ui.screens.budgets.api.BudgetListUiState
import java.util.Calendar
import javax.inject.Inject

data class BudgetListStateInput(
    val allBudgets: List<Budget>,
    val expenses: List<Expense>,
    val categories: List<Category>,
    val currentMonth: Int,
    val currentYear: Int
)

class BuildBudgetListStateUseCase @Inject constructor() {
    fun execute(input: BudgetListStateInput): BudgetListUiState.Success {
        val latestBudgets = input.allBudgets
            .groupBy { it.categoryId }
            .mapValues { (_, budgets) ->
                budgets.maxByOrNull { budget ->
                    budget.year * 12 + budget.month
                }
            }
            .values
            .filterNotNull()

        val currentMonthIndex = input.currentYear * 12 + input.currentMonth
        val activeBudgets = latestBudgets.filter { budget ->
            val budgetMonthIndex = budget.year * 12 + budget.month
            budgetMonthIndex <= currentMonthIndex
        }

        val (monthStart, monthEnd) = currentMonthRange(input.currentYear, input.currentMonth)
        val currentMonthExpenses = input.expenses.filter { it.date in monthStart..monthEnd }

        val budgetsWithProgress = activeBudgets.map { budget ->
            val spent = calculateSpentForBudget(budget, currentMonthExpenses)
            val category = budget.categoryId?.let { id -> input.categories.find { it.id == id } }
            val progress = budget.calculateProgress(spent)

            BudgetWithProgress(
                budget = budget,
                category = category,
                currentSpent = spent,
                progress = progress,
                shouldNotify75 = budget.shouldNotify75(spent),
                shouldNotify100 = budget.shouldNotify100(spent)
            )
        }.sortedByDescending { it.progress }

        return BudgetListUiState.Success(
            budgets = budgetsWithProgress,
            selectedMonth = input.currentMonth,
            selectedYear = input.currentYear,
            hasOverallBudget = activeBudgets.any { it.isOverallBudget() }
        )
    }

    private fun currentMonthRange(currentYear: Int, currentMonth: Int): Pair<Long, Long> {
        val start = Calendar.getInstance().apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, currentMonth - 1)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val end = Calendar.getInstance().apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, currentMonth - 1)
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        return start to end
    }

    private fun calculateSpentForBudget(budget: Budget, monthExpenses: List<Expense>): Long {
        return if (budget.categoryId != null) {
            monthExpenses
                .filter { it.categoryId == budget.categoryId }
                .sumOf { it.amount }
        } else {
            monthExpenses.sumOf { it.amount }
        }
    }
}
