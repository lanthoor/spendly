package `in`.co.spendly.workers

import android.app.Notification
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.pm.ServiceInfo
import android.database.Cursor
import android.provider.Telephony
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import `in`.co.spendly.domain.model.Expense
import `in`.co.spendly.domain.model.Income
import `in`.co.spendly.domain.repository.AccountRepository
import `in`.co.spendly.domain.repository.CategoryRepository
import `in`.co.spendly.domain.repository.ExpenseRepository
import `in`.co.spendly.domain.repository.IncomeRepository
import `in`.co.spendly.domain.repository.PreferencesRepository
import `in`.co.spendly.utils.SmsParser
import `in`.co.spendly.utils.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

/**
 * Worker to scan historical SMS messages on device and create transactions for eligible messages.
 *
 * - Respects known bank senders via SmsParser.isKnownBankSender
 * - Skips messages already represented in DB by matching sms_body + sms_timestamp
 * - Runs as foreground with a progress notification and supports cancel/pause/resume via broadcast receiver
 */
@HiltWorker
class HistoricalSmsScanWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val expenseRepository: ExpenseRepository,
    private val incomeRepository: IncomeRepository,
    private val categoryRepository: CategoryRepository,
    private val accountRepository: AccountRepository,
    private val preferencesRepository: PreferencesRepository
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "HistoricalSmsScanWorker"
        const val WORK_TAG = "HISTORICAL_SMS_SCAN"
        private const val NOTIFICATION_CHANNEL = "budget_alerts"
        private const val NOTIFICATION_ID = 3000
        const val ACTION_CANCEL = "in.co.spendly.ACTION_CANCEL_SMS_SCAN"
        const val ACTION_PAUSE = "in.co.spendly.ACTION_PAUSE_SMS_SCAN"
        const val ACTION_RESUME = "in.co.spendly.ACTION_RESUME_SMS_SCAN"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Build foreground notification
            setForegroundAsync(createForegroundInfo(0, 0))

            // If user disabled SMS auto-detection, exit
            val enabled = preferencesRepository.getSmsAutoDetectionEnabled().first()
            if (!enabled) return@withContext Result.success()

            // Load already-created SMS metadata from existing expenses/income (sms_body + sms_timestamp)
            val expenseSnapshot = expenseRepository.getAllExpenses().first()
            val incomeSnapshot = incomeRepository.getAllIncome().first()
            val seen = mutableSetOf<String>()
            expenseSnapshot.forEach { e ->
                if (e.smsBody != null && e.smsTimestamp != null) {
                    seen.add(hashKey(e.smsBody, e.smsTimestamp))
                }
            }
            incomeSnapshot.forEach { i ->
                if (i.smsBody != null && i.smsTimestamp != null) {
                    seen.add(hashKey(i.smsBody, i.smsTimestamp))
                }
            }

            // Query SMS inbox
            val resolver: ContentResolver = applicationContext.contentResolver
            val uri = Telephony.Sms.Inbox.CONTENT_URI
            val projection = arrayOf(
                Telephony.Sms._ID,
                Telephony.Sms.ADDRESS,
                Telephony.Sms.BODY,
                Telephony.Sms.DATE
            )

            val cursor: Cursor? = resolver.query(uri, projection, null, null, "date DESC")
            cursor ?: return@withContext Result.success()

            val totalCount = cursor.count
            var processed = 0
            var lastProgressUpdateTime = 0L
            val progressUpdateIntervalMs = 5000L // Update at most once per second

            while (cursor.moveToNext()) {
                val sender = cursor.getString(1) ?: ""
                val body = cursor.getString(2) ?: ""
                val date = cursor.getLong(3)

                // Progress update (debounced to 1 per second max)
                processed++
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastProgressUpdateTime >= progressUpdateIntervalMs) {
                    setProgress(workDataOf("progress" to processed, "total" to totalCount))
                    setForegroundAsync(createForegroundInfo(processed, totalCount))
                    lastProgressUpdateTime = currentTime
                }

                // Filter unknown senders
                if (!SmsParser.isKnownBankSender(sender)) continue

                // Skip if we already saw this sms (by body+timestamp)
                val key = hashKey(body, date)
                if (seen.contains(key)) continue

                // Attempt parse
                val parsed = SmsParser.parseBankSms(body, sender, date) ?: continue

                // Create transaction similar to SmsTransactionCreationWorker
                val defaultAccount =
                    accountRepository.getAllAccounts().firstOrNull()?.firstOrNull() ?: continue
                val categories = categoryRepository.getAllCategories().first()
                val defaultCategoryId = when (parsed.transactionType) {
                    TransactionType.EXPENSE -> 13L
                    TransactionType.INCOME -> 101L
                }
                val defaultCategory = categories.firstOrNull { it.id == defaultCategoryId }
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
                            smsTimestamp = date
                        )
                        expenseRepository.insertExpense(expense)
                    }

                    TransactionType.INCOME -> {
                        val income = Income(
                            id = 0,
                            amount = parsed.amount,
                            categoryId = defaultCategory?.id,
                            source = `in`.co.spendly.utils.IncomeSource.OTHER,
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
                            smsTimestamp = date
                        )
                        incomeRepository.insertIncome(income)
                    }
                }

                // Mark seen to avoid duplicates within this run
                seen.add(key)

                // Check for cancellation
                if (isStopped) {
                    cursor.close()
                    return@withContext Result.failure()
                }
            }

            cursor.close()
            // Final progress update & notification
            setProgress(workDataOf("progress" to totalCount, "total" to totalCount))
            setForegroundAsync(createForegroundInfo(totalCount, totalCount))

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Exception scanning historical SMS", e)
            Result.retry()
        }
    }

    private fun hashKey(body: String, timestamp: Long): String = "${body.hashCode()}_${timestamp}"

    private fun createForegroundInfo(processed: Int, total: Int): ForegroundInfo {
        val percent = if (total <= 0) 0 else (processed * 100 / total).coerceIn(0, 100)

        // Build cancel/pause/resume intents
        val cancelIntent = android.content.Intent(ACTION_CANCEL).apply {
            HistoricalSmsScanControlReceiver.putExtras(this)
        }
        val cancelPending = PendingIntent.getBroadcast(
            applicationContext,
            0,
            cancelIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pauseIntent = android.content.Intent(ACTION_PAUSE).apply {
            HistoricalSmsScanControlReceiver.putExtras(this)
        }
        val pausePending = PendingIntent.getBroadcast(
            applicationContext,
            1,
            pauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val resumeIntent = android.content.Intent(ACTION_RESUME).apply {
            HistoricalSmsScanControlReceiver.putExtras(this)
        }
        val resumePending = PendingIntent.getBroadcast(
            applicationContext,
            2,
            resumeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification =
            NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL)
                .setSmallIcon(android.R.drawable.stat_notify_sync)
                .setContentTitle("Scanning SMS for transactions")
                .setContentText("$percent% — scanning historical messages")
                .setProgress(100, percent, false)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Cancel", cancelPending)
                .addAction(android.R.drawable.ic_media_pause, "Pause", pausePending)
                .addAction(android.R.drawable.ic_media_play, "Resume", resumePending)
                .build()

        return ForegroundInfo(
            NOTIFICATION_ID,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        )
    }

    // Helper to easily create Data
    private fun workDataOf(vararg pairs: Pair<String, Any>): Data {
        val builder = Data.Builder()
        pairs.forEach { (k, v) ->
            when (v) {
                is Int -> builder.putInt(k, v)
                is Long -> builder.putLong(k, v)
                is String -> builder.putString(k, v)
                is Boolean -> builder.putBoolean(k, v)
            }
        }
        return builder.build()
    }
}
