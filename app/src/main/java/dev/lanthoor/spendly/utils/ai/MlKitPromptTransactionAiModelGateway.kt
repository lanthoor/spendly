package dev.lanthoor.spendly.utils.ai

import android.util.Log
import dev.lanthoor.spendly.core.model.preferences.AiModelAvailability
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MlKitPromptTransactionAiModelGateway @Inject constructor() : TransactionAiModelGateway {
    companion object {
        private const val TAG = "MlKitAiGateway"
    }

    override suspend fun checkAvailability(): AiModelAvailabilityResult {
        return try {
            val generationClass = Class.forName("com.google.mlkit.genai.prompt.Generation")
            val generationInstance = generationClass.getField("INSTANCE").get(null)
            val getClientMethod = generationClass.getMethod("getClient")
            val clientAny = getClientMethod.invoke(generationInstance)
                ?: throw IllegalStateException("ML Kit client unavailable")
            val client: Any = clientAny

            val checkStatusMethod = client.javaClass.methods.firstOrNull {
                it.name == "checkStatus" && it.parameterCount == 0
            }
            val getBaseModelNameMethod = client.javaClass.methods.firstOrNull {
                it.name == "getBaseModelName" && it.parameterCount == 0
            }

            val rawStatus = checkStatusMethod?.invoke(client)
            val baseModelName = getBaseModelNameMethod?.invoke(client) as? String

            val statusInt = when (rawStatus) {
                is Number -> rawStatus.toInt()
                else -> null
            }

            val availability = when (statusInt) {
                0 -> AiModelAvailability.UNAVAILABLE
                1 -> AiModelAvailability.DOWNLOADABLE
                2 -> AiModelAvailability.DOWNLOADING
                3 -> AiModelAvailability.AVAILABLE
                else -> AiModelAvailability.UNKNOWN
            }

            AiModelAvailabilityResult(
                availability = availability,
                baseModelName = baseModelName,
                errorCode = null
            )
        } catch (e: Exception) {
            Log.w(TAG, "checkAvailability failed", e)
            AiModelAvailabilityResult(
                availability = AiModelAvailability.UNAVAILABLE,
                baseModelName = null,
                errorCode = e.javaClass.simpleName
            )
        }
    }

    override suspend fun generate(prompt: String): AiGenerationResult {
        try {
            val generationClass = Class.forName("com.google.mlkit.genai.prompt.Generation")
            val generationInstance = generationClass.getField("INSTANCE").get(null)
            val getClientMethod = generationClass.getMethod("getClient")
            val clientAny = getClientMethod.invoke(generationInstance)
                ?: throw IllegalStateException("ML Kit client unavailable")
            val client: Any = clientAny

            val response = client.javaClass.getMethod("generateContent", String::class.java)
                .invoke(client, prompt)

            val text = extractResponseText(response)
            val modelName = tryGetBaseModelName(client)
            return AiGenerationResult(responseText = text, modelName = modelName)
        } catch (e: Exception) {
            throw IllegalStateException("ML Kit generation failed: ${e.message}", e)
        }
    }

    private fun tryGetBaseModelName(client: Any): String? {
        return try {
            client.javaClass.methods.firstOrNull {
                it.name == "getBaseModelName" && it.parameterCount == 0
            }?.invoke(client) as? String
        } catch (_: Exception) {
            null
        }
    }

    private fun extractResponseText(response: Any?): String {
        if (response == null) return ""

        val responseClass = response.javaClass
        val textField = responseClass.methods.firstOrNull {
            it.name == "getText" && it.parameterCount == 0
        }
        if (textField != null) {
            val value = textField.invoke(response) as? String
            if (!value.isNullOrBlank()) return value
        }

        val candidatesMethod = responseClass.methods.firstOrNull {
            it.name == "getCandidates" && it.parameterCount == 0
        }
        val candidates = candidatesMethod?.invoke(response)
        val firstCandidate = (candidates as? List<*>)?.firstOrNull()
        if (firstCandidate != null) {
            val candidateText = firstCandidate.javaClass.methods.firstOrNull {
                it.name == "getText" && it.parameterCount == 0
            }?.invoke(firstCandidate) as? String
            if (!candidateText.isNullOrBlank()) return candidateText
        }

        return response.toString()
    }
}
