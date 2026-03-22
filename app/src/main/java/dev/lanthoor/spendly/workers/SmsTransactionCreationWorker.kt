package dev.lanthoor.spendly.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dev.lanthoor.spendly.domain.model.Expense
import dev.lanthoor.spendly.domain.model.Income
import dev.lanthoor.spendly.domain.repository.AccountRepository
import dev.lanthoor.spendly.domain.repository.CategoryRepository
import dev.lanthoor.spendly.domain.repository.ExpenseRepository
import dev.lanthoor.spendly.domain.repository.IncomeRepository
import dev.lanthoor.spendly.domain.repository.PreferencesRepository
import dev.lanthoor.spendly.utils.IncomeSource
import dev.lanthoor.spendly.utils.SmsNotificationService
import dev.lanthoor.spendly.utils.SmsParser
import dev.lanthoor.spendly.utils.TransactionType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull

@HiltWorker
class SmsTransactionCreationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val expenseRepository: ExpenseRepository,
    private val incomeRepository: IncomeRepository,
    private val categoryRepository: CategoryRepository,
    private val accountRepository: AccountRepository,
    private val smsNotificationService: SmsNotificationService,
    private val preferencesRepository: PreferencesRepository
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "SmsTransactionCreationWorker"
        const val KEY_SENDER = "sender"
        const val KEY_BODY = "body"
        const val KEY_TIMESTAMP = "timestamp"
    }

    override suspend fun doWork(): Result {
        return try {
            // Check if SMS auto-detection is enabled in settings
            val autoDetectionEnabled = preferencesRepository.getSmsAutoDetectionEnabled().first()
            if (!autoDetectionEnabled) {
                return Result.success()  // Not an error, just disabled
            }

            // Extract input
            val sender = inputData.getString(KEY_SENDER) ?: return Result.failure()
            val body = inputData.getString(KEY_BODY) ?: return Result.failure()
            val timestamp = inputData.getLong(KEY_TIMESTAMP, System.currentTimeMillis())

            // Parse SMS
            val parsed = SmsParser.parseBankSms(body, sender, timestamp)
                ?: return Result.success()  // Not an error, just not a valid transaction

            // Get default account
            val defaultAccount = accountRepository.getAllAccounts().firstOrNull()?.firstOrNull()
                ?: return Result.failure()

            // Get default category based on transaction type
            // Default expense category: "Others" (ID 13)
            // Default income category: "Salary" (ID 101)
            val defaultCategoryId = when (parsed.transactionType) {
                TransactionType.EXPENSE -> 13L  // "Others"
                TransactionType.INCOME -> 101L  // "Salary"
            }
            val categories = categoryRepository.getAllCategories().first()
            val defaultCategory = categories.firstOrNull { it.id == defaultCategoryId }

            // Create transaction
            val now = System.currentTimeMillis()
            when (parsed.transactionType) {
                TransactionType.EXPENSE -> {
                    val expense = Expense(
                        id = 0,
                        amount = parsed.amount,
                        categoryId = defaultCategory?.id,
                        accountId = defaultAccount.id,
                        date = parsed.date,
                        description = parsed.description,
                        createdAt = now,
                        modifiedAt = now,
                        receipts = emptyList(),
                        smsSourceId = null,
                        smsBody = body,
                        smsConfidence = parsed.confidence,
                        smsTimestamp = timestamp
                    )
                    val expenseId = expenseRepository.insertExpense(expense)

                    // Send notification
                    smsNotificationService.showTransactionCreatedNotification(
                        applicationContext,
                        expenseId,
                        parsed
                    )
                }

                TransactionType.INCOME -> {
                    val income = Income(
                        id = 0,
                        amount = parsed.amount,
                        categoryId = defaultCategory?.id,
                        source = IncomeSource.OTHER,  // Default source
                        accountId = defaultAccount.id,
                        date = parsed.date,
                        description = parsed.description,
                        createdAt = now,
                        modifiedAt = now,
                        isRecurring = false,
                        linkedExpenseId = null,
                        smsSourceId = null,
                        smsBody = body,
                        smsConfidence = parsed.confidence,
                        smsTimestamp = timestamp
                    )
                    val incomeId = incomeRepository.insertIncome(income)

                    // Send notification
                    smsNotificationService.showTransactionCreatedNotification(
                        applicationContext,
                        incomeId,
                        parsed
                    )
                }
            }

            Result.success()

        } catch (e: Exception) {
            Log.e(TAG, "Exception during SMS transaction creation", e)
            Log.e(TAG, "Stack trace: ${e.stackTraceToString()}")
            Result.retry()
        }
    }
}
