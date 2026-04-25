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
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MlKitPromptTransactionAiModelGateway @Inject constructor() : TransactionAiModelGateway {
    companion object {
        private const val TAG = "MlKitAiGateway"
    }

    override suspend fun checkAvailability(): AiModelAvailabilityResult {
        return try {
            Log.d(TAG, "checkAvailability: begin")
            val client = Generation.getClient()
            Log.d(TAG, "checkAvailability: client class=${client.javaClass.name}")

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
                startModelDownload(client)
            } else {
                initialAvailability
            }

            Log.d(
                TAG,
                "checkAvailability: mapped status=$status to availability=$resolvedAvailability baseModel=$baseModelName"
            )

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
            Log.d(TAG, "generate: begin promptLength=${prompt.length}")
            val client = Generation.getClient()
            Log.d(TAG, "generate: client class=${client.javaClass.name}")

            val response = client.generateContent(prompt)
            Log.d(TAG, "generate: response candidates=${response.candidates.size}")

            val text = extractResponseText(response)
            val modelName = runCatching { client.getBaseModelName() }.getOrNull()
            Log.d(
                TAG,
                "generate: completed modelName=$modelName responseTextLength=${text.length}"
            )
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

    private suspend fun startModelDownload(client: com.google.mlkit.genai.prompt.GenerativeModel): AiModelAvailability {
        Log.d(TAG, "checkAvailability: status DOWNLOADABLE, starting model download")
        val downloadStatus = client.download().first()
        Log.d(TAG, "checkAvailability: first download status=$downloadStatus")

        return when (downloadStatus) {
            is DownloadStatus.DownloadStarted -> AiModelAvailability.DOWNLOADING
            is DownloadStatus.DownloadProgress -> AiModelAvailability.DOWNLOADING
            is DownloadStatus.DownloadCompleted -> AiModelAvailability.AVAILABLE
            is DownloadStatus.DownloadFailed -> {
                Log.w(
                    TAG,
                    "checkAvailability: download failed errorCode=${downloadStatus.e.errorCode}",
                    downloadStatus.e
                )
                availabilityFromError(mapGenAiErrorCode(downloadStatus.e.errorCode))
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
