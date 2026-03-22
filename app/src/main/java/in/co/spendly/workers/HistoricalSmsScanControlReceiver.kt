package `in`.co.spendly.workers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.WorkManager

/**
 * Simple BroadcastReceiver to control historical SMS scan from notification actions.
 * Actions handled:
 * - ACTION_CANCEL: Cancels work tagged with WORK_TAG
 * - ACTION_PAUSE: Cancels work tagged with WORK_TAG (treated as pause)
 * - ACTION_RESUME: Enqueues a new HistoricalSmsScanWorker (treated as resume)
 */
class HistoricalSmsScanControlReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "HistoricalSmsCtrlRecv"

        fun putExtras(intent: Intent) {
            // placeholder for symmetry; kept for future use
        }
    }

    override fun onReceive(context: Context, intent: Intent?) {
        try {
            val action = intent?.action ?: return
            val wm = WorkManager.getInstance(context)
            when (action) {
                HistoricalSmsScanWorker.ACTION_CANCEL,
                HistoricalSmsScanWorker.ACTION_PAUSE -> {
                    wm.cancelAllWorkByTag(HistoricalSmsScanWorker.WORK_TAG)
                }

                HistoricalSmsScanWorker.ACTION_RESUME -> {
                    // Enqueue a new scan with the same tag
                    val request = androidx.work.OneTimeWorkRequestBuilder<HistoricalSmsScanWorker>()
                        .addTag(HistoricalSmsScanWorker.WORK_TAG)
                        .build()
                    wm.enqueue(request)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling control broadcast", e)
        }
    }
}
