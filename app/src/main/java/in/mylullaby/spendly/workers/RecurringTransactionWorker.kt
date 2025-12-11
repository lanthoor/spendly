package `in`.mylullaby.spendly.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import `in`.mylullaby.spendly.utils.RecurringTransactionProcessor

/**
 * WorkManager worker that processes recurring transactions daily.
 * Runs at midnight to check for due recurring transactions and create new entries.
 *
 * Uses Hilt for dependency injection with @HiltWorker.
 */
@HiltWorker
class RecurringTransactionWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val recurringTransactionProcessor: RecurringTransactionProcessor
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // Process all recurring transactions
            recurringTransactionProcessor.processRecurringTransactions()

            // Return success
            Result.success()
        } catch (e: Exception) {
            // Log error and retry
            e.printStackTrace()
            Result.retry()
        }
    }
}
