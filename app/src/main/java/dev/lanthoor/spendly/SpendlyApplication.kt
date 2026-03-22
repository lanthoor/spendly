package dev.lanthoor.spendly

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp
import dev.lanthoor.spendly.workers.BudgetNotificationWorker
import dev.lanthoor.spendly.workers.RecurringTransactionWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Application class for Spendly expense tracker.
 *
 * Annotated with @HiltAndroidApp to enable Hilt dependency injection.
 * Schedules background WorkManager jobs for recurring transactions and budget notifications.
 *
 * Note: App initialization (seeding categories/accounts, processing recurring transactions)
 * is now handled by InitializationRepository/ViewModel to ensure data is ready before UI loads.
 */
@HiltAndroidApp
class SpendlyApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        // Schedule background workers
        // Note: Initialization is now handled by InitializationViewModel
        scheduleRecurringTransactionWorker()
        scheduleBudgetNotificationWorker()
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

    /**
     * Schedules a periodic WorkManager job to check budgets and send notifications.
     * Runs every 6 hours (4 times per day).
     */
    private fun scheduleBudgetNotificationWorker() {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true) // Only run when battery is not low
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED) // Offline-only app
            .build()

        val budgetWorkRequest = PeriodicWorkRequestBuilder<BudgetNotificationWorker>(
            6, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "budget_notifications",
            ExistingPeriodicWorkPolicy.KEEP,
            budgetWorkRequest
        )
    }
}
