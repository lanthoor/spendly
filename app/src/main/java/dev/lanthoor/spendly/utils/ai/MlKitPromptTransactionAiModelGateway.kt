package dev.lanthoor.spendly.utils.ai

import android.util.Log
import com.google.mlkit.genai.common.DownloadStatus
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.common.GenAiException
import com.google.mlkit.genai.prompt.Generation
import dev.lanthoor.spendly.core.model.preferences.AiModelAvailability
import dev.lanthoor.spendly.domain.model.ai.AiGenerationResult
import dev.lanthoor.spendly.domain.model.ai.AiModelAvailabilityResult
import dev.lanthoor.spendly.domain.repository.TransactionAiModelGateway
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MlKitPromptTransactionAiModelGateway @Inject constructor() : TransactionAiModelGateway {
    companion object {
        private const val TAG = "MlKitAiGateway"
    }

    private val gatewayScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val downloadMutex = Mutex()
    @Volatile
    private var isDownloadInProgress: Boolean = false

    override suspend fun checkAvailability(): AiModelAvailabilityResult {
        return try {
            val client = Generation.getClient()

            val status = client.checkStatus()
            val baseModelName = runCatching { client.getBaseModelName() }.getOrNull()
            val initialAvailability = when (status) {
                FeatureStatus.UNAVAILABLE -> AiModelAvailability.UNAVAILABLE
                FeatureStatus.DOWNLOADABLE -> AiModelAvailability.DOWNLOADABLE
                FeatureStatus.DOWNLOADING -> AiModelAvailability.DOWNLOADING
                FeatureStatus.AVAILABLE -> AiModelAvailability.AVAILABLE
                else -> AiModelAvailability.UNKNOWN
            }

            val resolvedAvailability = if (initialAvailability == AiModelAvailability.DOWNLOADABLE) {
                startModelDownloadIfNeeded(client)
                AiModelAvailability.DOWNLOADING
            } else {
                initialAvailability
            }

            AiModelAvailabilityResult(
                availability = resolvedAvailability,
                baseModelName = baseModelName,
                errorCode = null
            )
        } catch (e: GenAiException) {
            Log.w(TAG, "checkAvailability failed with GenAiException", e)
            val mappedError = mapGenAiErrorCode(e.errorCode)
            AiModelAvailabilityResult(
                availability = availabilityFromError(mappedError),
                baseModelName = null,
                errorCode = mappedError
            )
        } catch (e: Exception) {
            Log.w(TAG, "checkAvailability failed", e)
            AiModelAvailabilityResult(
                availability = AiModelAvailability.UNKNOWN,
                baseModelName = null,
                errorCode = e.javaClass.simpleName
            )
        }
    }

    override suspend fun generate(prompt: String): AiGenerationResult {
        try {
            val client = Generation.getClient()

            val response = client.generateContent(prompt)

            val text = extractResponseText(response)
            val modelName = runCatching { client.getBaseModelName() }.getOrNull()
            return AiGenerationResult(responseText = text, modelName = modelName)
        } catch (e: GenAiException) {
            Log.w(TAG, "generate failed with GenAiException", e)
            val mapped = mapGenAiErrorCode(e.errorCode)
            throw IllegalStateException("ML Kit generation failed: $mapped", e)
        } catch (e: Exception) {
            Log.w(TAG, "generate failed", e)
            throw IllegalStateException("ML Kit generation failed: ${e.message}", e)
        }
    }

    private fun extractResponseText(response: com.google.mlkit.genai.prompt.GenerateContentResponse): String {
        return response.candidates.firstOrNull()?.text.orEmpty()
    }

    private fun startModelDownloadIfNeeded(client: com.google.mlkit.genai.prompt.GenerativeModel) {
        if (isDownloadInProgress) return

        gatewayScope.launch {
            downloadMutex.withLock {
                if (isDownloadInProgress) return@withLock

                isDownloadInProgress = true
                Log.i(TAG, "Starting on-device model download")
                try {
                    client.download().collect { downloadStatus ->
                        when (downloadStatus) {
                            is DownloadStatus.DownloadStarted -> {
                                Log.i(TAG, "Model download started")
                            }

                            is DownloadStatus.DownloadProgress -> Unit

                            is DownloadStatus.DownloadCompleted -> {
                                Log.i(TAG, "Model download completed")
                            }

                            is DownloadStatus.DownloadFailed -> {
                                val mapped = mapGenAiErrorCode(downloadStatus.e.errorCode)
                                Log.w(
                                    TAG,
                                    "Model download failed: $mapped",
                                    downloadStatus.e
                                )
                            }
                        }
                    }
                } catch (e: GenAiException) {
                    Log.w(TAG, "Model download failed with GenAiException", e)
                } catch (e: Exception) {
                    Log.w(TAG, "Model download failed", e)
                } finally {
                    isDownloadInProgress = false
                }
            }
        }
    }

    private fun availabilityFromError(errorCode: String): AiModelAvailability {
        return when (errorCode) {
            "NOT_AVAILABLE",
            "NEEDS_SYSTEM_UPDATE",
            "AICORE_INCOMPATIBLE",
            "FEATURE_NOT_FOUND",
            "UNSUPPORTED_DEVICE" -> AiModelAvailability.UNAVAILABLE

            else -> AiModelAvailability.UNKNOWN
        }
    }

    private fun mapGenAiErrorCode(errorCode: Int): String {
        return when (errorCode) {
            GenAiException.ErrorCode.PER_APP_BATTERY_USE_QUOTA_EXCEEDED -> "QUOTA_EXCEEDED"
            GenAiException.ErrorCode.BUSY -> "RATE_LIMIT_EXCEEDED"
            GenAiException.ErrorCode.BACKGROUND_USE_BLOCKED -> "BACKGROUND_USE_BLOCKED"
            GenAiException.ErrorCode.NOT_AVAILABLE -> "NOT_AVAILABLE"
            GenAiException.ErrorCode.NEEDS_SYSTEM_UPDATE -> "NEEDS_SYSTEM_UPDATE"
            GenAiException.ErrorCode.AICORE_INCOMPATIBLE -> "AICORE_INCOMPATIBLE"
            606 -> "FEATURE_NOT_FOUND"
            else -> "GENAI_ERROR_$errorCode"
        }
    }
}
