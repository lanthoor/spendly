package dev.lanthoor.spendly.ui.screens.expenses

import dev.lanthoor.spendly.domain.model.Account
import dev.lanthoor.spendly.domain.model.Expense
import dev.lanthoor.spendly.domain.repository.ExpenseRepository
import dev.lanthoor.spendly.utils.CurrencyUtils

class ExpenseEditorService(
    private val expenseRepository: ExpenseRepository
) {
    suspend fun saveExpense(state: ExpenseFormState): Result<Long> {
        val amountInPaise = CurrencyUtils.parseRupeesToPaise(state.amount)
        val currentTime = System.currentTimeMillis()

        val expense = Expense(
            id = state.id,
            amount = amountInPaise,
            categoryId = state.categoryId,
            date = state.date,
            description = state.description.trim(),
            accountId = state.accountId,
            createdAt = state.createdAt ?: currentTime,
            modifiedAt = currentTime,
            smsSourceId = state.smsSourceId,
            smsBody = state.smsBody,
            smsConfidence = state.smsConfidence,
            smsTimestamp = state.smsTimestamp
        )

        return if (state.isEditMode) {
            expenseRepository.updateExpense(expense)
            Result.success(expense.id)
        } else {
            val id = expenseRepository.insertExpense(expense)
            Result.success(id)
        }
    }

    suspend fun deleteExpense(id: Long): Result<Unit> {
        return try {
            val expense = Expense(
                id = id,
                amount = 0,
                categoryId = null,
                date = 0,
                description = "",
                accountId = Account.DEFAULT_ACCOUNT_ID,
                createdAt = 0,
                modifiedAt = 0
            )
            expenseRepository.deleteExpense(expense)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
