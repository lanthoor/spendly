package dev.lanthoor.spendly.domain.usecase.transactions

import android.util.Log
import dev.lanthoor.spendly.domain.model.TransactionAiEnrichmentUpdate
import dev.lanthoor.spendly.domain.repository.ExpenseRepository
import dev.lanthoor.spendly.domain.repository.IncomeRepository
import dev.lanthoor.spendly.domain.repository.PreferencesRepository
import dev.lanthoor.spendly.domain.repository.TransactionAiEnrichmentRepository
import dev.lanthoor.spendly.core.model.finance.AiEnrichmentStatus
import dev.lanthoor.spendly.core.model.finance.TransactionType
import dev.lanthoor.spendly.core.model.preferences.AiModelAvailability
import dev.lanthoor.spendly.core.model.preferences.AiEnrichmentSettings
import dev.lanthoor.spendly.core.model.preferences.AiPromptVersion
import dev.lanthoor.spendly.utils.ai.AiEnrichmentParser
import dev.lanthoor.spendly.utils.ai.AiEnrichmentPromptBuilder
import dev.lanthoor.spendly.utils.ai.AiPromptBatchResponse
import dev.lanthoor.spendly.utils.ai.TransactionAiModelGateway
import dev.lanthoor.spendly.utils.ai.TransactionEnrichmentCandidate
import kotlinx.coroutines.flow.first
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import javax.inject.Inject

data class EnrichSmsTransactionsResult(
    val attempted: Int,
    val enriched: Int,
    val failed: Int,
    val skipped: Int
)

class EnrichSmsTransactionsUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val incomeRepository: IncomeRepository,
    private val preferencesRepository: PreferencesRepository,
    private val enrichmentRepository: TransactionAiEnrichmentRepository,
    private val aiGateway: TransactionAiModelGateway
) {
    companion object {
        private const val TAG = "EnrichSmsTxUseCase"
        private const val MAX_SMS_CHAR_BUDGET = 8000
    }

    private val json = Json {
        ignoreUnknownKeys = true
    }

    suspend fun runForTransactionIds(
        expenseIds: List<Long>,
        incomeIds: List<Long>
    ): EnrichSmsTransactionsResult {
        val settings = normalizeSettings(preferencesRepository.getAiEnrichmentSettings().first())
        if (!settings.enabled) {
            return EnrichSmsTransactionsResult(0, 0, 0, expenseIds.size + incomeIds.size)
        }

        val availabilityResult = aiGateway.checkAvailability()
        preferencesRepository.setAiModelAvailability(
            availability = availabilityResult.availability,
            checkedAt = System.currentTimeMillis(),
            baseModelName = availabilityResult.baseModelName,
            lastErrorCode = availabilityResult.errorCode
        )

        if (availabilityResult.availability != AiModelAvailability.AVAILABLE) {
            return EnrichSmsTransactionsResult(0, 0, 0, expenseIds.size + incomeIds.size)
        }

        val candidates = loadCandidates(expenseIds, incomeIds)
        if (candidates.isEmpty()) {
            return EnrichSmsTransactionsResult(0, 0, 0, 0)
        }

        val pendingCandidates = filterPendingCandidates(candidates, settings)
        if (pendingCandidates.isEmpty()) {
            return EnrichSmsTransactionsResult(0, 0, 0, candidates.size)
        }

        var attempted = 0
        var enriched = 0
        var failed = 0

        val batches = splitIntoBatches(pendingCandidates, settings.batchSize)
        batches.forEachIndexed { index, batch ->
            val batchId = "${System.currentTimeMillis()}-$index"
            val prompt = AiEnrichmentPromptBuilder.buildPrompt(batchId, batch)
            attempted += batch.size

            val updates = try {
                val generation = aiGateway.generate(prompt)
                val parsed = parseResponse(generation.responseText)
                val now = System.currentTimeMillis()
                buildUpdatesFromResponse(
                    candidates = batch,
                    response = parsed,
                    promptVersion = settings.promptVersion,
                    modelName = generation.modelName ?: availabilityResult.baseModelName,
                    enrichedAt = now
                )
            } catch (e: Exception) {
                safeLogWarn("Batch enrichment failed", e)
                batch.map {
                    AiEnrichmentParser.failedUpdate(
                        candidate = it,
                        promptVersion = settings.promptVersion,
                        reason = "batch-error:${e.javaClass.simpleName}",
                        modelName = availabilityResult.baseModelName
                    )
                }
            }

            enrichmentRepository.applyUpdates(updates)
            enriched += updates.count { it.status == AiEnrichmentStatus.ENRICHED }
            failed += updates.count { it.status == AiEnrichmentStatus.FAILED }
        }

        return EnrichSmsTransactionsResult(
            attempted = attempted,
            enriched = enriched,
            failed = failed,
            skipped = candidates.size - pendingCandidates.size
        )
    }

    suspend fun runForAllSmsTransactions(): EnrichSmsTransactionsResult {
        val expenseIds = expenseRepository.getSmsLinkedExpensesSince(0L).map { it.id }
        val incomeIds = incomeRepository.getSmsLinkedIncomeSince(0L).map { it.id }
        return runForTransactionIds(expenseIds = expenseIds, incomeIds = incomeIds)
    }

    suspend fun markImportedPendingIfMissing() {
        val settings = normalizeSettings(preferencesRepository.getAiEnrichmentSettings().first())
        val expenses = expenseRepository.getSmsLinkedExpensesSince(0L)
        val incomes = incomeRepository.getSmsLinkedIncomeSince(0L)

        expenses.forEach { expense ->
            enrichmentRepository.upsertPending(
                transactionType = TransactionType.EXPENSE,
                transactionId = expense.id,
                promptVersion = settings.promptVersion
            )
        }
        incomes.forEach { income ->
            enrichmentRepository.upsertPending(
                transactionType = TransactionType.INCOME,
                transactionId = income.id,
                promptVersion = settings.promptVersion
            )
        }
    }

    suspend fun markPending(transactionType: TransactionType, transactionId: Long) {
        val settings = normalizeSettings(preferencesRepository.getAiEnrichmentSettings().first())
        enrichmentRepository.upsertPending(
            transactionType = transactionType,
            transactionId = transactionId,
            promptVersion = settings.promptVersion
        )
    }

    private suspend fun loadCandidates(
        expenseIds: List<Long>,
        incomeIds: List<Long>
    ): List<TransactionEnrichmentCandidate> {
        val expenses = expenseRepository.getExpensesByIds(expenseIds)
        val incomes = incomeRepository.getIncomeByIds(incomeIds)

        val expenseCandidates = expenses.mapNotNull { expense ->
            val smsBody = expense.smsBody ?: return@mapNotNull null
            val smsTimestamp = expense.smsTimestamp ?: return@mapNotNull null
            TransactionEnrichmentCandidate(
                transactionType = TransactionType.EXPENSE,
                transactionId = expense.id,
                amount = expense.amount,
                regexDescription = expense.description,
                smsBody = smsBody,
                smsSender = extractSenderFromSmsBody(smsBody),
                smsTimestamp = smsTimestamp
            )
        }

        val incomeCandidates = incomes.mapNotNull { income ->
            val smsBody = income.smsBody ?: return@mapNotNull null
            val smsTimestamp = income.smsTimestamp ?: return@mapNotNull null
            TransactionEnrichmentCandidate(
                transactionType = TransactionType.INCOME,
                transactionId = income.id,
                amount = income.amount,
                regexDescription = income.description,
                smsBody = smsBody,
                smsSender = extractSenderFromSmsBody(smsBody),
                smsTimestamp = smsTimestamp
            )
        }

        return expenseCandidates + incomeCandidates
    }

    private suspend fun filterPendingCandidates(
        candidates: List<TransactionEnrichmentCandidate>,
        settings: AiEnrichmentSettings
    ): List<TransactionEnrichmentCandidate> {
        val result = mutableListOf<TransactionEnrichmentCandidate>()
        candidates.forEach { candidate ->
            val existing = enrichmentRepository.getByTransaction(
                transactionType = candidate.transactionType,
                transactionId = candidate.transactionId
            )
            if (existing == null || existing.status != AiEnrichmentStatus.ENRICHED) {
                enrichmentRepository.upsertPending(
                    transactionType = candidate.transactionType,
                    transactionId = candidate.transactionId,
                    promptVersion = settings.promptVersion
                )
                result += candidate
            }
        }
        return result
    }

    private fun splitIntoBatches(
        candidates: List<TransactionEnrichmentCandidate>,
        batchSize: Int
    ): List<List<TransactionEnrichmentCandidate>> {
        if (candidates.isEmpty()) return emptyList()
        val configuredSize = batchSize.coerceAtLeast(1)
        val batches = mutableListOf<List<TransactionEnrichmentCandidate>>()
        var current = mutableListOf<TransactionEnrichmentCandidate>()
        var currentCharCount = 0

        candidates.forEach { candidate ->
            val candidateChars = candidate.smsBody.length + candidate.regexDescription.length + candidate.smsSender.length
            val exceedBatchSize = current.size >= configuredSize
            val exceedCharBudget = current.isNotEmpty() && (currentCharCount + candidateChars) > MAX_SMS_CHAR_BUDGET

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

    private fun parseResponse(responseText: String): AiPromptBatchResponse {
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

    private fun buildUpdatesFromResponse(
        candidates: List<TransactionEnrichmentCandidate>,
        response: AiPromptBatchResponse,
        promptVersion: Int,
        modelName: String?,
        enrichedAt: Long
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
                    promptVersion = promptVersion,
                    modelName = modelName,
                    enrichedAt = enrichedAt
                )
            }
        }
    }

    private fun extractSenderFromSmsBody(smsBody: String): String {
        return smsBody.take(80)
    }

    private suspend fun normalizeSettings(settings: AiEnrichmentSettings): AiEnrichmentSettings {
        if (settings.promptVersion == AiPromptVersion.CURRENT) return settings
        preferencesRepository.setAiPromptVersion(AiPromptVersion.CURRENT)
        return settings.copy(promptVersion = AiPromptVersion.CURRENT)
    }

    private fun safeLogWarn(message: String, throwable: Throwable) {
        runCatching { Log.w(TAG, message, throwable) }
    }
}
