package `in`.mylullaby.spendly

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import `in`.mylullaby.spendly.di.ApplicationScope
import `in`.mylullaby.spendly.domain.repository.CategoryRepository
import `in`.mylullaby.spendly.utils.RecurringTransactionProcessor
import `in`.mylullaby.spendly.workers.RecurringTransactionWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Application class for Spendly expense tracker.
 *
 * Annotated with @HiltAndroidApp to enable Hilt dependency injection.
 * Seeds predefined categories on first launch.
 * Processes recurring transactions at startup.
 * Schedules daily WorkManager job for recurring transactions.
 */
@HiltAndroidApp
class SpendlyApplication : Application() {

    @Inject
    lateinit var categoryRepository: CategoryRepository

    @Inject
    lateinit var recurringTransactionProcessor: RecurringTransactionProcessor

    @Inject
    @ApplicationScope
    lateinit var coroutineScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()

        coroutineScope.launch {
            try {
                // Seed predefined categories on first launch
                if (!categoryRepository.isPredefinedSeeded()) {
                    categoryRepository.seedPredefinedCategories()
                }

                // Process recurring transactions at startup (check last 3 months)
                recurringTransactionProcessor.processRecurringTransactions()
            } catch (e: Exception) {
                // Log error but don't crash the app
                // In production, this would use proper logging (Timber, etc.)
                e.printStackTrace()
            }
        }

        // Schedule daily recurring transaction worker
        scheduleRecurringTransactionWorker()
    }

    /**
     * Schedules a daily WorkManager job to process recurring transactions.
     * Runs once per day at midnight.
     */
    private fun scheduleRecurringTransactionWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED) // Offline-only app
            .build()

        // Calculate delay to midnight
        val currentTime = Calendar.getInstance()
        val midnight = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val delayToMidnight = midnight.timeInMillis - currentTime.timeInMillis

        val recurringWorkRequest = PeriodicWorkRequestBuilder<RecurringTransactionWorker>(
            1, TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .setInitialDelay(delayToMidnight, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "recurring_transactions",
            ExistingPeriodicWorkPolicy.KEEP,
            recurringWorkRequest
        )
    }
}
