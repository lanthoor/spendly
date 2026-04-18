package dev.lanthoor.spendly.ui.screens.budgets.usecase

import dev.lanthoor.spendly.domain.usecase.budgets.BudgetListStateInput
import dev.lanthoor.spendly.domain.usecase.budgets.BuildBudgetListStateUseCase
import dev.lanthoor.spendly.domain.model.Budget
import dev.lanthoor.spendly.domain.model.Category
import dev.lanthoor.spendly.domain.model.Expense
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class BuildBudgetListStateUseCaseTest {

    private val useCase = BuildBudgetListStateUseCase()

    @Test
    fun `execute keeps latest active budget per category and computes progress`() {
        val currentMonth = 6
        val currentYear = 2026

        val categories = listOf(
            Category(1, "Food", "restaurant", 0, false, 1),
            Category(2, "Travel", "flight", 0, false, 2)
        )

        val allBudgets = listOf(
            Budget(id = 1, categoryId = 1, amount = 3_000_00L, month = 4, year = 2026),
            Budget(id = 2, categoryId = 1, amount = 4_000_00L, month = 6, year = 2026),
            Budget(id = 3, categoryId = null, amount = 8_000_00L, month = 5, year = 2026),
            Budget(id = 4, categoryId = 2, amount = 2_000_00L, month = 7, year = 2026)
        )

        val expenses = listOf(
            expense(1_500_00L, categoryId = 1L, year = 2026, month = 6, day = 5),
            expense(1_000_00L, categoryId = 1L, year = 2026, month = 6, day = 15),
            expense(500_00L, categoryId = 2L, year = 2026, month = 6, day = 16)
        )

        val result = useCase.execute(
            BudgetListStateInput(
                allBudgets = allBudgets,
                expenses = expenses,
                categories = categories,
                currentMonth = currentMonth,
                currentYear = currentYear
            )
        )

        assertEquals(2, result.budgets.size)
        val categoryBudget = result.budgets.first { it.budget.categoryId == 1L }
        assertEquals(2, categoryBudget.budget.id)
        assertEquals(2_500_00L, categoryBudget.currentSpent)
        assertTrue(result.hasOverallBudget)
        assertEquals(currentMonth, result.selectedMonth)
        assertEquals(currentYear, result.selectedYear)
    }

    private fun expense(amount: Long, categoryId: Long, year: Int, month: Int, day: Int): Expense {
        val date = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        return Expense(
            amount = amount,
            categoryId = categoryId,
            date = date,
            description = "expense",
            accountId = 1L,
            createdAt = date,
            modifiedAt = date
        )
    }
}
