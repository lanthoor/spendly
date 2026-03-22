package `in`.co.spendly.utils

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.co.spendly.MainActivity
import `in`.co.spendly.R
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for showing notifications when SMS transactions are detected.
 *
 * Reuses the existing "budget_alerts" notification channel created by BudgetNotificationService.
 */
@Singleton
class SmsNotificationService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "budget_alerts"  // Reuse existing channel
        private const val SMS_NOTIFICATION_ID_BASE = 2000
        private const val DEBOUNCE_MILLIS = 1000L
    }

    private val lastNotificationTimestamps = mutableMapOf<Int, Long>()

    /**
     * Shows a notification for a newly created SMS transaction.
     *
     * @param context Application context
     * @param transactionId The created transaction ID (expense or income)
     * @param parsed The parsed transaction data
     */
    fun showTransactionCreatedNotification(
        context: Context,
        transactionId: Long,
        parsed: ParsedTransaction
    ) {
        val notificationId = SMS_NOTIFICATION_ID_BASE + transactionId.toInt()
        val now = System.currentTimeMillis()
        val lastTime = lastNotificationTimestamps[notificationId] ?: 0L
        if (now - lastTime < DEBOUNCE_MILLIS) {
            return
        }
        lastNotificationTimestamps[notificationId] = now

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Build notification text
        val typeEmoji = if (parsed.transactionType == TransactionType.EXPENSE) "💸" else "💰"
        val amountStr = CurrencyUtils.paiseToRupeeString(parsed.amount)
        val title = context.getString(R.string.notification_transaction_added, typeEmoji)
        val actionWord =
            if (parsed.transactionType == TransactionType.EXPENSE) context.getString(R.string.label_spent) else context.getString(
                R.string.label_received
            )
        val merchantPart =
            parsed.merchantName?.let { context.getString(R.string.msg_at_merchant, it) } ?: ""
        val text = "$amountStr $actionWord$merchantPart"

        // Create intent to open main app
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            transactionId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build notification
        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        // Send notification
        notificationManager.notify(notificationId, notification)
    }
}
