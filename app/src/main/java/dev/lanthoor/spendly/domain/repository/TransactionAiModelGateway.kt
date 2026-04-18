package dev.lanthoor.spendly.domain.repository

import dev.lanthoor.spendly.domain.model.ai.AiGenerationResult
import dev.lanthoor.spendly.domain.model.ai.AiModelAvailabilityResult

interface TransactionAiModelGateway {
    suspend fun checkAvailability(): AiModelAvailabilityResult

    suspend fun generate(prompt: String): AiGenerationResult
}
