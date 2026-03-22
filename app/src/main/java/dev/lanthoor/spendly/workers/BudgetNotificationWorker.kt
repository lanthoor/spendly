package dev.lanthoor.spendly.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dev.lanthoor.spendly.utils.BudgetNotificationService

/**
 * WorkManager worker that periodically checks budgets and sends notifications.
 *
 * This worker is scheduled to run every 6 hours to check if any budgets have reached
 * their 75% or 100% spending thresholds. It delegates the actual notification logic
 * to BudgetNotificationService.
 *
 * Scheduled in SpendlyApplication with:
 * - Period: 6 hours (4 checks per day)
 * - Constraints: Battery not low
 * - Policy: KEEP existing work
 */
@HiltWorker
class BudgetNotificationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val budgetNotificationService: BudgetNotificationService
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "BudgetNotificationWorker"
    }

    /**
     * Executes the worker task: check budgets and send notifications.
     *
     * @return Result.success() if notifications checked successfully,
     *         Result.retry() if an error occurred
     */
    override suspend fun doWork(): Result {
        return try {
            budgetNotificationService.checkBudgetsAndNotify()
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create budget notifications", e)
            Result.retry()
        }
    }
}
