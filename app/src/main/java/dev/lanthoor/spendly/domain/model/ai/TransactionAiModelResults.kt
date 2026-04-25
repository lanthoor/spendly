package dev.lanthoor.spendly.domain.model.ai

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
