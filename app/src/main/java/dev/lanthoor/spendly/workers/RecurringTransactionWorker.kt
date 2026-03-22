package dev.lanthoor.spendly.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dev.lanthoor.spendly.utils.RecurringTransactionProcessor

@HiltWorker
class RecurringTransactionWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val recurringTransactionProcessor: RecurringTransactionProcessor
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "RecurringTransactionWorker"
    }

    override suspend fun doWork(): Result {
        return try {
            recurringTransactionProcessor.processRecurringTransactions()
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to process recurring transactions", e)
            Result.retry()
        }
    }
}
