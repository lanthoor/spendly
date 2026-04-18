package dev.lanthoor.spendly.ui.screens.analytics.usecase

import dev.lanthoor.spendly.core.model.finance.IncomeSource
import dev.lanthoor.spendly.domain.model.Category
import dev.lanthoor.spendly.domain.model.Expense
import dev.lanthoor.spendly.domain.model.Income
import dev.lanthoor.spendly.domain.usecase.analytics.AnalyticsPeriod
import dev.lanthoor.spendly.domain.usecase.analytics.AnalyticsStateInput
import dev.lanthoor.spendly.domain.usecase.analytics.BuildAnalyticsStateUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class BuildAnalyticsStateUseCaseTest {

    private val useCase = BuildAnalyticsStateUseCase()

    @Test
    fun `execute builds analytics state for calendar year`() {
        val now = Calendar.getInstance()
        val currentYear = now.get(Calendar.YEAR)

        val inCurrentYear = dateOf(currentYear, Calendar.MARCH, 10)
        val inPreviousYear = dateOf(currentYear - 1, Calendar.MARCH, 10)

        val result = useCase.execute(
            AnalyticsStateInput(
                expenses = listOf(
                    expense(1_500_00L, inCurrentYear),
                    expense(900_00L, inPreviousYear)
                ),
                incomes = listOf(
                    income(5_000_00L, inCurrentYear),
                    income(2_000_00L, inPreviousYear)
                ),
                categories = listOf(
                    Category(1, "Food", "restaurant", 0, false, 1)
                ),
                period = AnalyticsPeriod.CALENDAR_YEAR
            )
        )

        assertEquals(1_500_00L, result.totalExpense)
        assertEquals(5_000_00L, result.totalIncome)
        assertEquals(3_500_00L, result.netBalance)
        assertFalse(result.expenseTrendData.isEmpty())
        assertFalse(result.incomeTrendData.isEmpty())
        assertFalse(result.netWorthTrendData.isEmpty())
        assertEquals(1, result.categories.size)
    }

    @Test
    fun `execute returns 100 percent increase when previous totals are zero`() {
        val now = Calendar.getInstance()
        val currentYear = now.get(Calendar.YEAR)
        val inCurrentYear = dateOf(currentYear, Calendar.JANUARY, 15)

        val result = useCase.execute(
            AnalyticsStateInput(
                expenses = listOf(expense(1_000_00L, inCurrentYear)),
                incomes = emptyList(),
                categories = emptyList(),
                period = AnalyticsPeriod.CALENDAR_YEAR
            )
        )

        assertEquals(100f, result.expenseChange)
        assertTrue(result.incomeChange == 0f)
    }

    private fun expense(amount: Long, date: Long) = Expense(
        amount = amount,
        categoryId = 1L,
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

    private fun dateOf(year: Int, month: Int, day: Int): Long {
        return Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}
