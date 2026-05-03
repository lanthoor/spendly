package dev.lanthoor.spendly.domain.usecase.transactions

import android.util.Log
import dev.lanthoor.spendly.domain.model.TransactionAiEnrichmentUpdate
import dev.lanthoor.spendly.domain.model.ai.AiPromptBatchResponse
import dev.lanthoor.spendly.domain.model.ai.TransactionEnrichmentCandidate
import dev.lanthoor.spendly.domain.repository.CategoryRepository
import dev.lanthoor.spendly.domain.repository.ExpenseRepository
import dev.lanthoor.spendly.domain.repository.IncomeRepository
import dev.lanthoor.spendly.domain.repository.PreferencesRepository
import dev.lanthoor.spendly.domain.repository.TransactionAiModelGateway
import dev.lanthoor.spendly.domain.repository.TransactionAiEnrichmentRepository
import dev.lanthoor.spendly.core.model.finance.AiEnrichmentStatus
import dev.lanthoor.spendly.core.model.finance.TransactionType
import dev.lanthoor.spendly.core.model.preferences.AiModelAvailability
import dev.lanthoor.spendly.core.model.preferences.AiEnrichmentSettings
import dev.lanthoor.spendly.core.model.preferences.AiPromptVersion
import java.util.Locale
import kotlinx.coroutines.flow.first
import javax.inject.Inject

data class EnrichSmsTransactionsResult(
    val attempted: Int,
    val enriched: Int,
    val failed: Int,
    val skipped: Int
)

class EnrichSmsTransactionsUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val expenseRepository: ExpenseRepository,
    private val incomeRepository: IncomeRepository,
    private val preferencesRepository: PreferencesRepository,
    private val enrichmentRepository: TransactionAiEnrichmentRepository,
    private val aiGateway: TransactionAiModelGateway,
    private val aiEnrichmentEngine: AiEnrichmentEngine
) {
    companion object {
        private const val TAG = "EnrichSmsTxUseCase"
        private const val MAX_SMS_CHAR_BUDGET = 8000
    }

    suspend fun refreshModelAvailability() {
        val availabilityResult = aiGateway.checkAvailability()
        preferencesRepository.setAiModelAvailability(
            availability = availabilityResult.availability,
            checkedAt = System.currentTimeMillis(),
            baseModelName = availabilityResult.baseModelName,
            lastErrorCode = availabilityResult.errorCode
        )
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

        val categoryLookup = buildCategoryLookup()
        val allowedCategories = categoryLookup.keys.sorted()

        var attempted = 0
        var enriched = 0
        var failed = 0

        val batches = aiEnrichmentEngine.splitIntoBatches(pendingCandidates, settings.batchSize)
        safeLogDebug("runForTransactionIds: batches=${batches.size}")
        batches.forEachIndexed { index, batch ->
            val batchId = "${System.currentTimeMillis()}-$index"
            val prompt = AiEnrichmentPromptBuilder.buildPrompt(batchId, batch, allowedCategories)
            attempted += batch.size

            val updates = try {
                val generation = aiGateway.generate(prompt)
                val parsed = aiEnrichmentEngine.parseResponse(generation.responseText)
                val now = System.currentTimeMillis()
                safeLogDebug(
                    TAG,
                    "runForTransactionIds: batch[$index] generation ok model=${generation.modelName}, " +
                            "responseLength=${generation.responseText.length}, parsedResults=${parsed.results.size}"
                )
                preferencesRepository.setAiModelAvailability(
                    availability = availabilityResult.availability,
                    checkedAt = now,
                    baseModelName = generation.modelName ?: availabilityResult.baseModelName,
                    lastErrorCode = null
                )
                aiEnrichmentEngine.buildUpdatesFromResponse(
                    candidates = batch,
                    response = parsed,
                    categoryLookup = categoryLookup,
                    promptVersion = settings.promptVersion,
                    modelName = generation.modelName ?: availabilityResult.baseModelName,
                    enrichedAt = now,
                    categoryResolver = { name, lookup -> resolveCategoryId(name, lookup) }
                )
            } catch (e: Exception) {
                safeLogWarn("Batch enrichment failed", e)
                safeLogWarn("runForTransactionIds: batch[$index] failed ${e.javaClass.simpleName}: ${e.message}", e)
                preferencesRepository.setAiModelAvailability(
                    availability = availabilityResult.availability,
                    checkedAt = System.currentTimeMillis(),
                    baseModelName = availabilityResult.baseModelName,
                    lastErrorCode = classifyAiErrorCode(e)
                )
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
            applyCategoryUpdates(batch, updates)
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

    private suspend fun applyCategoryUpdates(
        candidates: List<TransactionEnrichmentCandidate>,
        updates: List<TransactionAiEnrichmentUpdate>
    ) {
        val byTxKey = updates.associateBy { "${it.transactionType.name}:${it.transactionId}" }

        candidates.forEach { candidate ->
            val update = byTxKey[candidate.txKey] ?: return@forEach
            val categoryId = update.categoryId ?: return@forEach

            when (candidate.transactionType) {
                TransactionType.EXPENSE -> {
                    val expense = expenseRepository.getExpensesByIds(listOf(candidate.transactionId)).firstOrNull()
                        ?: return@forEach
                    if (expense.categoryId != categoryId) {
                        expenseRepository.updateExpense(expense.copy(categoryId = categoryId))
                    }
                }

                TransactionType.INCOME -> {
                    val income = incomeRepository.getIncomeByIds(listOf(candidate.transactionId)).firstOrNull()
                        ?: return@forEach
                    if (income.categoryId != categoryId) {
                        incomeRepository.updateIncome(income.copy(categoryId = categoryId))
                    }
                }
            }
        }
    }

    private suspend fun buildCategoryLookup(): Map<String, Long> {
        return categoryRepository.getAllCategories().first()
            .associate { it.name.lowercase(Locale.ROOT) to it.id }
    }

    private fun resolveCategoryId(categoryName: String?, categoryLookup: Map<String, Long>): Long? {
        val key = categoryName?.trim()?.lowercase(Locale.ROOT).orEmpty()
        if (key.isBlank()) return null
        return categoryLookup[key]
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

    private fun safeLogDebug(message: String) {
        runCatching { Log.d(TAG, message) }
    }

    private fun safeLogDebug(tag: String, message: String) {
        runCatching { Log.d(tag, message) }
    }

    private fun classifyAiErrorCode(throwable: Throwable): String {
        val details = buildList {
            var current: Throwable? = throwable
            while (current != null) {
                add(current.javaClass.simpleName)
                current.message?.let { add(it) }
                current = current.cause
            }
        }.joinToString(" ").uppercase(Locale.ROOT)

        return when {
            "QUOTA" in details -> "QUOTA_EXCEEDED"
            "RATE" in details || "TOO_MANY_REQUESTS" in details || "RESOURCE_EXHAUSTED" in details -> "RATE_LIMIT_EXCEEDED"
            "BACKGROUND_USE_BLOCKED" in details -> "BACKGROUND_USE_BLOCKED"
            else -> throwable.javaClass.simpleName
        }
    }
}
