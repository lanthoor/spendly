package dev.lanthoor.spendly.domain.repository

import dev.lanthoor.spendly.core.model.finance.AiEnrichmentStatus
import dev.lanthoor.spendly.core.model.finance.TransactionType
import dev.lanthoor.spendly.domain.model.TransactionAiEnrichment
import dev.lanthoor.spendly.domain.model.TransactionAiEnrichmentUpdate
import kotlinx.coroutines.flow.Flow

interface TransactionAiEnrichmentRepository {
    suspend fun upsert(entry: TransactionAiEnrichment)

    suspend fun upsertPending(
        transactionType: TransactionType,
        transactionId: Long,
        promptVersion: Int
    )

    suspend fun upsertAll(entries: List<TransactionAiEnrichment>)

    suspend fun applyUpdates(updates: List<TransactionAiEnrichmentUpdate>)

    suspend fun getByTransaction(
        transactionType: TransactionType,
        transactionId: Long
    ): TransactionAiEnrichment?

    fun observeByTransaction(
        transactionType: TransactionType,
        transactionId: Long
    ): Flow<TransactionAiEnrichment?>

    suspend fun getByStatuses(
        statuses: Set<AiEnrichmentStatus>,
        limit: Int
    ): List<TransactionAiEnrichment>

    suspend fun getByStatusesForMixedTransactions(
        statuses: Set<AiEnrichmentStatus>,
        expenseIds: List<Long>,
        incomeIds: List<Long>,
        limit: Int
    ): List<TransactionAiEnrichment>

    fun observeAll(): Flow<List<TransactionAiEnrichment>>
}
