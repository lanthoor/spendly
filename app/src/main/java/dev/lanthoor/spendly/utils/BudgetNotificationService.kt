package dev.lanthoor.spendly.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.lanthoor.spendly.MainActivity
import dev.lanthoor.spendly.R
import dev.lanthoor.spendly.domain.model.Budget
import dev.lanthoor.spendly.domain.model.Category
import dev.lanthoor.spendly.domain.repository.BudgetRepository
import dev.lanthoor.spendly.domain.repository.CategoryRepository
import dev.lanthoor.spendly.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service responsible for checking budget thresholds and sending notifications.
 *
 * This service monitors user budgets and sends Android notifications when spending
 * reaches 75% or 100% of the budget amount. Notifications are sent once per threshold
 * per budget and reset monthly.
 */
@Singleton
class BudgetNotificationService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val budgetRepository: BudgetRepository,
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository
) {
    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "budget_alerts"
        private const val NOTIFICATION_ID_BASE = 1000
        private const val DEBOUNCE_MILLIS = 1000L
    }

    private val lastNotificationTimestamps = mutableMapOf<Int, Long>()

    /**
     * Checks all budgets for the current month and sends notifications if thresholds are met.
     *
     * For each budget:
     * 1. Calculate total spending for the budget period
     * 2. Check if 75% or 100% threshold is reached
     * 3. Send notification if threshold met and notification not already sent
     * 4. Update notification flag in database
     */
    suspend fun checkBudgetsAndNotify() {
        // Create notification channel (safe to call multiple times)
        createNotificationChannel()

        // Get current month and year
        val now = LocalDate.now()
        val currentMonth = now.monthValue
        val currentYear = now.year

        // Get all budgets for current month
        val budgets = budgetRepository.getBudgetsForMonth(currentMonth, currentYear).first()

        // Check each budget
        for (budget in budgets) {
            // Calculate spent amount for this budget
            val spent = calculateSpentForBudget(budget, currentMonth, currentYear)
            val progress = budget.calculateProgress(spent)

            // Check 100% threshold first (more critical)
            if (progress >= 100f && budget.shouldNotify100(spent)) {
                // Get category (null for overall budget)
                val category =
                    budget.categoryId?.let { categoryRepository.getCategoryById(it).first() }
                sendNotification(budget, category, progress, is75Percent = false)
                budgetRepository.markNotification100Sent(budget.id)
            }
            // Check 75% threshold
            else if (progress >= 75f && budget.shouldNotify75(spent)) {
                val category =
                    budget.categoryId?.let { categoryRepository.getCategoryById(it).first() }
                sendNotification(budget, category, progress, is75Percent = true)
                budgetRepository.markNotification75Sent(budget.id)
            }
        }
    }

    /**
     * Calculates total spending for a budget in the given month/year.
     *
     * @param budget The budget to calculate spending for
     * @param month The month (1-12)
     * @param year The year
     * @return Total spent amount in paise
     */
    private suspend fun calculateSpentForBudget(
        budget: Budget,
        month: Int,
        year: Int
    ): Long {
        // Calculate start and end timestamps for the month
        val startDate = LocalDate.of(year, month, 1)
        val endDate = startDate.plusMonths(1).minusDays(1)

        val startTimestamp = startDate.atStartOfDay().toEpochSecond(java.time.ZoneOffset.UTC) * 1000
        val endTimestamp = endDate.atTime(23, 59, 59).toEpochSecond(java.time.ZoneOffset.UTC) * 1000

        return if (budget.categoryId != null) {
            // Category-specific budget - filter by category
            expenseRepository.getExpensesByDateRange(
                startTimestamp,
                endTimestamp
            ).first()
                .filter { it.categoryId == budget.categoryId }
                .sumOf { it.amount }
        } else {
            // Overall budget (all categories)
            expenseRepository.getExpensesByDateRange(
                startTimestamp,
                endTimestamp
            ).first().sumOf { it.amount }
        }
    }

    /**
     * Sends a budget alert notification.
     *
     * @param budget The budget that triggered the alert
     * @param category The category (null for overall budget)
     * @param progress The current spending progress (0-100+)
     * @param is75Percent True if this is a 75% alert, false for 100% alert
     */
    private fun sendNotification(
        budget: Budget,
        category: Category?,
        progress: Float,
        is75Percent: Boolean
    ) {
        val notificationId = NOTIFICATION_ID_BASE + budget.id.toInt()
        val now = System.currentTimeMillis()
        val lastTime = lastNotificationTimestamps[notificationId] ?: 0L
        if (now - lastTime < DEBOUNCE_MILLIS) {
            return
        }
        lastNotificationTimestamps[notificationId] = now

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Build notification title and text
        val categoryName = category?.name ?: context.getString(R.string.label_overall)
        val threshold = if (is75Percent) "75%" else "100%"
        val title = context.getString(R.string.notification_budget_alert_title, categoryName)
        val text = context.getString(
            R.string.notification_budget_alert_text,
            threshold,
            budget.displayAmount()
        )

        // Create intent to open Budget List screen
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "budgets")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            budget.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build notification
        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Use app icon
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        // Send notification (use unique ID per budget)
        notificationManager.notify(notificationId, notification)
    }

    /**
     * Creates the notification channel for budget alerts.
     * Required for Android 8.0+ (API 26+).
     */
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.notification_channel_description)
            enableVibration(true)
        }

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    /**
     * Resets all notification flags for all budgets.
     * Should be called on the first day of each month to allow new notifications.
     */
    suspend fun resetMonthlyNotificationFlags() {
        budgetRepository.resetMonthlyNotificationFlags()
    }
}
