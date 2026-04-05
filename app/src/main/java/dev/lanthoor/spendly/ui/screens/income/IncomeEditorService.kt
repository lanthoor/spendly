package dev.lanthoor.spendly.ui.screens.income

import dev.lanthoor.spendly.core.model.finance.IncomeSource
import dev.lanthoor.spendly.domain.model.Account
import dev.lanthoor.spendly.domain.model.Income
import dev.lanthoor.spendly.domain.repository.IncomeRepository
import dev.lanthoor.spendly.utils.CurrencyUtils

class IncomeEditorService(
    private val incomeRepository: IncomeRepository
) {
    suspend fun saveIncome(state: IncomeFormState): Result<Long> {
        val amountInPaise = CurrencyUtils.parseRupeesToPaise(state.amount)
        val currentTime = System.currentTimeMillis()

        val income = Income(
            id = state.id,
            amount = amountInPaise,
            categoryId = state.selectedCategory?.id,
            accountId = state.accountId,
            source = state.source,
            date = state.date,
            description = state.description.trim(),
            isRecurring = state.isRecurring,
            linkedExpenseId = state.linkedExpenseId,
            createdAt = state.createdAt ?: currentTime,
            modifiedAt = currentTime,
            smsSourceId = state.smsSourceId,
            smsBody = state.smsBody,
            smsConfidence = state.smsConfidence,
            smsTimestamp = state.smsTimestamp
        )

        return if (state.isEditMode) {
            incomeRepository.updateIncome(income)
            Result.success(income.id)
        } else {
            val id = incomeRepository.insertIncome(income)
            Result.success(id)
        }
    }

    suspend fun deleteIncome(id: Long): Result<Unit> {
        return try {
            val income = Income(
                id = id,
                amount = 0,
                categoryId = null,
                accountId = Account.DEFAULT_ACCOUNT_ID,
                source = IncomeSource.OTHER,
                date = 0,
                description = "",
                isRecurring = false,
                linkedExpenseId = null,
                createdAt = 0,
                modifiedAt = 0
            )
            incomeRepository.deleteIncome(income)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
