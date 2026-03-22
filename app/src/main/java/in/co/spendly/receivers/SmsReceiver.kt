package `in`.co.spendly.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import `in`.co.spendly.workers.SmsTransactionCreationWorker

/**
 * BroadcastReceiver that intercepts incoming SMS messages for transaction detection.
 *
 * This receiver listens for SMS_RECEIVED broadcasts and enqueues a WorkManager job
 * to parse the SMS in the background. The actual SMS processing and transaction
 * creation happens in SmsTransactionCreationWorker.
 *
 * **Note:** This receiver is always enabled at the system level for simplicity.
 * The user's "Enable SMS auto-detection" preference toggle is enforced in
 * SmsTransactionCreationWorker, which checks the DataStore setting before processing.
 *
 * This architecture allows the receiver to remain stateless (no DataStore access),
 * while the Worker uses Hilt dependency injection to access PreferencesRepository.
 *
 * **Logging:**
 * All steps are logged to logcat under TAG "SmsReceiver"
 * Use `adb logcat | grep SmsReceiver` to monitor
 */
class SmsReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "SmsReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        // Check if this is an SMS_RECEIVED broadcast
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            return
        }

        // Extract SMS messages from intent (can be multiple parts for long SMS)
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isEmpty()) {
            return
        }

        // Concatenate all parts into a single message body
        // Long SMS (>160 chars) are split into multiple parts by Android
        val sender = messages[0].originatingAddress
        val timestamp = messages[0].timestampMillis
        val fullBody = messages.joinToString("") { it.messageBody ?: "" }

        if (sender == null || fullBody.isBlank()) {
            return
        }

        // Enqueue background worker to parse SMS
        val workRequest = OneTimeWorkRequestBuilder<SmsTransactionCreationWorker>()
            .setInputData(
                workDataOf(
                    SmsTransactionCreationWorker.KEY_SENDER to sender,
                    SmsTransactionCreationWorker.KEY_BODY to fullBody,
                    SmsTransactionCreationWorker.KEY_TIMESTAMP to timestamp
                )
            )
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }
}
