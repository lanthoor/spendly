package dev.lanthoor.spendly.workers

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
import dev.lanthoor.spendly.domain.model.Expense
import dev.lanthoor.spendly.domain.model.Income
import dev.lanthoor.spendly.domain.repository.AccountRepository
import dev.lanthoor.spendly.domain.repository.CategoryRepository
import dev.lanthoor.spendly.domain.repository.ExpenseRepository
import dev.lanthoor.spendly.domain.repository.IncomeRepository
import dev.lanthoor.spendly.domain.repository.PreferencesRepository
import dev.lanthoor.spendly.utils.SmsAccountMatcher
import dev.lanthoor.spendly.utils.SmsDuplicateDetector
import dev.lanthoor.spendly.utils.SmsFingerprintFactory
import dev.lanthoor.spendly.utils.SmsFingerprintPreload
import dev.lanthoor.spendly.utils.SmsCategoryMatcher
import dev.lanthoor.spendly.utils.SmsParser
import dev.lanthoor.spendly.utils.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

/**
 * Worker to scan historical SMS messages on device and create transactions for eligible messages.
 *
 * - Respects known bank senders via SmsParser.isKnownBankSender
 * - Skips messages already represented in DB by strict/semantic SMS fingerprints
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
        const val ACTION_CANCEL = "dev.lanthoor.spendly.ACTION_CANCEL_SMS_SCAN"
        const val ACTION_PAUSE = "dev.lanthoor.spendly.ACTION_PAUSE_SMS_SCAN"
        const val ACTION_RESUME = "dev.lanthoor.spendly.ACTION_RESUME_SMS_SCAN"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Build foreground notification
            setForegroundAsync(createForegroundInfo(0, 0))

            // If user disabled SMS auto-detection, exit
            val enabled = preferencesRepository.getSmsAutoDetectionEnabled().first()
            if (!enabled) return@withContext Result.success()

            // Load already-created SMS metadata from existing expenses/income
            val expenseSnapshot = expenseRepository.getSmsLinkedExpensesSince(0L)
            val incomeSnapshot = incomeRepository.getSmsLinkedIncomeSince(0L)
            val duplicateDetector = SmsDuplicateDetector()

            val existingFingerprints =
                SmsFingerprintPreload.fromExpenses(expenseSnapshot) +
                    SmsFingerprintPreload.fromIncome(incomeSnapshot)
            duplicateDetector.preload(existingFingerprints)

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
            val progressUpdateIntervalMs = 5000L // Update at most once per 5 seconds
            val accounts = accountRepository.getAllAccounts().firstOrNull().orEmpty()
            val defaultAccountId = SmsAccountMatcher.resolveDefaultAccountId(accounts)
            if (defaultAccountId == null) {
                cursor.close()
                return@withContext Result.success()
            }
            val categories = categoryRepository.getAllCategories().first()
            val categoryLookup = SmsCategoryMatcher.buildCategoryLookup(categories)

            while (cursor.moveToNext()) {
                val sender = cursor.getString(1) ?: ""
                val body = cursor.getString(2) ?: ""
                val date = cursor.getLong(3)

                // Progress update (debounced to once per 5 seconds)
                processed++
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastProgressUpdateTime >= progressUpdateIntervalMs) {
                    setProgress(workDataOf("progress" to processed, "total" to totalCount))
                    setForegroundAsync(createForegroundInfo(processed, totalCount))
                    lastProgressUpdateTime = currentTime
                }

                if (!SmsParser.isKnownBankSender(sender)) continue

                // Attempt parse
                val parsed = SmsParser.parseBankSms(body, sender, date) ?: continue

                val fingerprint = SmsFingerprintFactory.create(
                    sender = sender,
                    body = body,
                    timestamp = date,
                    parsed = parsed
                )
                val duplicateReason = duplicateDetector.findDuplicateReason(fingerprint)
                if (duplicateReason != null) {
                    Log.d(TAG, "dedup hit ${duplicateReason.name.lowercase()}")
                    continue
                }

                // Create transaction similar to SmsTransactionCreationWorker
                val categoryResolution = SmsCategoryMatcher.resolveCategory(
                    parsed = parsed,
                    smsBody = body,
                    sender = sender,
                    categoryLookup = categoryLookup
                )

                if (categoryResolution.matchedCategory != null || categoryResolution.category == null) {
                    Log.d(
                        TAG,
                        "Category selected=${categoryResolution.category?.name ?: "None"} type=${parsed.transactionType} reason=${categoryResolution.reason}"
                    )
                }
                val now = System.currentTimeMillis()
                val matchedAccountId = SmsAccountMatcher.resolveAccountId(
                    accounts = accounts,
                    parsed = parsed,
                    sender = sender,
                    body = body
                )
                val accountId = matchedAccountId ?: defaultAccountId

                when (parsed.transactionType) {
                    TransactionType.EXPENSE -> {
                        val expense = Expense(
                            id = 0,
                            amount = parsed.amount,
                            categoryId = categoryResolution.category?.id,
                            accountId = accountId,
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
                            categoryId = categoryResolution.category?.id,
                            source = dev.lanthoor.spendly.utils.IncomeSource.OTHER,
                            accountId = accountId,
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
                duplicateDetector.markSeen(fingerprint)

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
