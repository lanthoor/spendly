package dev.lanthoor.spendly.core.model.finance

import dev.lanthoor.spendly.domain.model.Expense
import dev.lanthoor.spendly.domain.model.Income

sealed class RecentTransaction {
    data class ExpenseTransaction(val expense: Expense) : RecentTransaction()
    data class IncomeTransaction(val income: Income) : RecentTransaction()
}
