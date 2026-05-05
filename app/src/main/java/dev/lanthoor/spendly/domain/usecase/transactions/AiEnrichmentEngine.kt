package dev.lanthoor.spendly.domain.usecase.transactions

import dev.lanthoor.spendly.domain.model.TransactionAiEnrichmentUpdate
import dev.lanthoor.spendly.domain.model.ai.AiPromptBatchResponse
import dev.lanthoor.spendly.domain.model.ai.TransactionEnrichmentCandidate
import dev.lanthoor.spendly.core.model.finance.AiEnrichmentStatus
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiEnrichmentEngine @Inject constructor() {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    /**
     * Robustly extracts and parses JSON from a potentially messy LLM response.
     */
    fun parseResponse(responseText: String): AiPromptBatchResponse {
        return try {
            json.decodeFromString(AiPromptBatchResponse.serializer(), responseText)
        } catch (e: SerializationException) {
            val extractedJson = extractJsonObject(responseText)
            json.decodeFromString(AiPromptBatchResponse.serializer(), extractedJson)
        }
    }

    private fun extractJsonObject(value: String): String {
        val start = value.indexOf('{')
        val end = value.lastIndexOf('}')
        if (start >= 0 && end > start) {
            return value.substring(start, end + 1)
        }
        throw SerializationException("No JSON object found in model response")
    }

    /**
     * Logic to split candidates into batches based on character limits and batch size.
     */
    fun splitIntoBatches(
        candidates: List<TransactionEnrichmentCandidate>,
        batchSize: Int,
        maxCharBudget: Int = 8000
    ): List<List<TransactionEnrichmentCandidate>> {
        if (candidates.isEmpty()) return emptyList()
        val configuredSize = batchSize.coerceAtLeast(1)
        val batches = mutableListOf<List<TransactionEnrichmentCandidate>>()
        var current = mutableListOf<TransactionEnrichmentCandidate>()
        var currentCharCount = 0

        candidates.forEach { candidate ->
            val candidateChars = candidate.smsBody.length + candidate.regexDescription.length + candidate.smsSender.length
            val exceedBatchSize = current.size >= configuredSize
            val exceedCharBudget = current.isNotEmpty() && (currentCharCount + candidateChars) > maxCharBudget

            if (exceedBatchSize || exceedCharBudget) {
                batches += current.toList()
                current = mutableListOf()
                currentCharCount = 0
            }

            current += candidate
            currentCharCount += candidateChars
        }

        if (current.isNotEmpty()) {
            batches += current.toList()
        }

        return batches
    }

    fun buildUpdatesFromResponse(
        candidates: List<TransactionEnrichmentCandidate>,
        response: AiPromptBatchResponse,
        categoryLookup: Map<String, Long>,
        promptVersion: Int,
        modelName: String?,
        enrichedAt: Long,
        categoryResolver: (String, Map<String, Long>) -> Long?
    ): List<TransactionAiEnrichmentUpdate> {
        val byTxKey = response.results.associateBy { it.txKey }

        return candidates.map { candidate ->
            val result = byTxKey[candidate.txKey]
            if (result == null) {
                AiEnrichmentParser.failedUpdate(
                    candidate = candidate,
                    promptVersion = promptVersion,
                    reason = "missing-result",
                    modelName = modelName
                )
            } else {
                AiEnrichmentParser.toUpdate(
                    candidate = candidate,
                    result = result,
                    resolvedCategoryId = categoryResolver(result.categoryName ?: "", categoryLookup),
                    promptVersion = promptVersion,
                    modelName = modelName,
                    enrichedAt = enrichedAt
                )
            }
        }
    }
}
