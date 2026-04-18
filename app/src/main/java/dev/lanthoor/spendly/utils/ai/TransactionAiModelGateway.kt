package dev.lanthoor.spendly.utils.ai

import dev.lanthoor.spendly.core.model.preferences.AiModelAvailability

data class AiModelAvailabilityResult(
    val availability: AiModelAvailability,
    val baseModelName: String?,
    val errorCode: String?
)

data class AiGenerationResult(
    val responseText: String,
    val modelName: String?
)

interface TransactionAiModelGateway {
    suspend fun checkAvailability(): AiModelAvailabilityResult

    suspend fun generate(prompt: String): AiGenerationResult
}
