package dev.lanthoor.spendly.ui.screens.dashboard.usecase

import dev.lanthoor.spendly.core.model.finance.IncomeSource
import dev.lanthoor.spendly.core.model.preferences.YearType
import dev.lanthoor.spendly.domain.model.Budget
import dev.lanthoor.spendly.domain.model.Category
import dev.lanthoor.spendly.domain.model.Expense
import dev.lanthoor.spendly.domain.model.Income
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class BuildDashboardSummaryUseCaseTest {

    private val useCase = BuildDashboardSummaryUseCase()

    @Test
    fun `execute computes month and ytd summary for calendar year`() {
        val selectedYear = 2026
        val selectedMonth = 6

        val mayDate = monthDate(selectedYear, 5, 10)
        val juneDate = monthDate(selectedYear, 6, 5)
        val julyDate = monthDate(selectedYear, 7, 2)

        val expenses = listOf(
            expense(amount = 1_000_00L, date = mayDate, categoryId = 1L),
            expense(amount = 2_500_00L, date = juneDate, categoryId = 1L),
            expense(amount = 500_00L, date = juneDate, categoryId = 2L),
            expense(amount = 900_00L, date = julyDate, categoryId = 1L)
        )
        val incomes = listOf(
            income(amount = 8_000_00L, date = mayDate),
            income(amount = 10_000_00L, date = juneDate),
            income(amount = 7_000_00L, date = julyDate)
        )
        val categories = listOf(
            Category(id = 1, name = "Food", icon = "food", color = 0, isCustom = false, sortOrder = 1),
            Category(id = 2, name = "Travel", icon = "travel", color = 0, isCustom = false, sortOrder = 2)
        )
        val budgets = listOf(
            Budget(amount = 4_000_00L, month = selectedMonth, year = selectedYear, categoryId = 1L),
            Budget(amount = 5_000_00L, month = selectedMonth, year = selectedYear, categoryId = null)
        )

        val result = useCase.execute(
            DashboardSummaryInput(
                expenses = expenses,
                incomes = incomes,
                categories = categories,
                allBudgets = budgets,
                selectedMonth = selectedMonth,
                selectedYear = selectedYear,
                yearType = YearType.CALENDAR
            )
        )

        assertEquals(3_000_00L, result.financialSummary.monthExpenses)
        assertEquals(10_000_00L, result.financialSummary.monthIncome)
        assertEquals(7_000_00L, result.financialSummary.monthNetBalance)
        assertEquals(2_500_00L, result.budgets.first { it.budget.categoryId == 1L }.currentSpent)
        assertEquals(3, result.recentTransactions.size)
        assertEquals(1L, result.topCategories.first().category.id)
        assertTrue(result.hasTransactions)
    }

    @Test
    fun `execute returns 100 percent change when previous period is zero`() {
        val selectedYear = 2026
        val selectedMonth = 1
        val januaryDate = monthDate(selectedYear, 1, 12)

        val result = useCase.execute(
            DashboardSummaryInput(
                expenses = listOf(expense(amount = 1_000_00L, date = januaryDate, categoryId = 1L)),
                incomes = emptyList(),
                categories = emptyList(),
                allBudgets = emptyList(),
                selectedMonth = selectedMonth,
                selectedYear = selectedYear,
                yearType = YearType.CALENDAR
            )
        )

        assertEquals(100f, result.financialSummary.monthExpenseChange)
    }

    private fun expense(amount: Long, date: Long, categoryId: Long?) = Expense(
        amount = amount,
        categoryId = categoryId,
        date = date,
        description = "expense",
        accountId = 1L,
        createdAt = date,
        modifiedAt = date
    )

    private fun income(amount: Long, date: Long) = Income(
        amount = amount,
        categoryId = null,
        source = IncomeSource.SALARY,
        date = date,
        description = "income",
        accountId = 1L,
        createdAt = date,
        modifiedAt = date
    )

    private fun monthDate(year: Int, month: Int, day: Int): Long {
        return Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}
