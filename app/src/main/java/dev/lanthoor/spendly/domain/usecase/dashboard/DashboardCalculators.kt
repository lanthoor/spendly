package dev.lanthoor.spendly.domain.usecase.dashboard

import dev.lanthoor.spendly.core.model.finance.BudgetWithProgress
import dev.lanthoor.spendly.core.model.finance.RecentTransaction
import dev.lanthoor.spendly.domain.model.Budget
import dev.lanthoor.spendly.domain.model.Category
import dev.lanthoor.spendly.domain.model.Expense
import dev.lanthoor.spendly.domain.model.Income

object DashboardCalculators {
    fun calculatePercentageChange(previous: Long, current: Long): Float {
        if (previous == 0L) {
            return if (current > 0) 100f else 0f
        }
        return ((current - previous).toFloat() / previous.toFloat()) * 100f
    }

    fun buildRecentTransactions(
        expenses: List<Expense>,
        incomes: List<Income>
    ): List<RecentTransaction> {
        val expenseTransactions = expenses.map { expense ->
            RecentTransaction.ExpenseTransaction(expense)
        }
        val incomeTransactions = incomes.map { income ->
            RecentTransaction.IncomeTransaction(income)
        }
        return (expenseTransactions + incomeTransactions)
            .sortedByDescending {
                when (it) {
                    is RecentTransaction.ExpenseTransaction -> it.expense.date
                    is RecentTransaction.IncomeTransaction -> it.income.date
                }
            }
    }

    fun getTopCategories(
        expenses: List<Expense>,
        categories: List<Category>
    ): List<DashboardCategorySpending> {
        val categoryMap = categories.associateBy { it.id }

        return expenses
            .groupBy { it.categoryId }
            .mapNotNull { (categoryId, groupedExpenses) ->
                categoryId?.let { id ->
                    categoryMap[id]?.let { category ->
                        DashboardCategorySpending(
                            category = category,
                            totalAmount = groupedExpenses.sumOf { it.amount },
                            transactionCount = groupedExpenses.size
                        )
                    }
                }
            }
            .sortedByDescending { it.totalAmount }
    }

    fun toBudgetsWithProgress(
        allBudgets: List<Budget>,
        selectedMonth: Int,
        selectedYear: Int,
        categories: List<Category>,
        monthExpenses: List<Expense>
    ): List<BudgetWithProgress> {
        val monthBudgets =
            allBudgets.filter { it.month == selectedMonth && it.year == selectedYear }

        return monthBudgets.map { budget ->
            val spent = calculateSpentForBudget(budget, monthExpenses)
            val category = budget.categoryId?.let { id -> categories.find { it.id == id } }
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
