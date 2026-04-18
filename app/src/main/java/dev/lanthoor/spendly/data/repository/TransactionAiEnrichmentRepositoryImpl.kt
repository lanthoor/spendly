package dev.lanthoor.spendly.data.repository

import dev.lanthoor.spendly.data.local.dao.TransactionAiEnrichmentDao
import dev.lanthoor.spendly.data.local.entities.TransactionAiEnrichmentEntity
import dev.lanthoor.spendly.domain.model.TransactionAiEnrichment
import dev.lanthoor.spendly.domain.model.TransactionAiEnrichmentUpdate
import dev.lanthoor.spendly.domain.repository.TransactionAiEnrichmentRepository
import dev.lanthoor.spendly.core.model.finance.AiEnrichmentStatus
import dev.lanthoor.spendly.core.model.finance.CounterpartyIdentifierType
import dev.lanthoor.spendly.core.model.finance.CounterpartyRole
import dev.lanthoor.spendly.core.model.finance.CounterpartyType
import dev.lanthoor.spendly.core.model.finance.PaymentRail
import dev.lanthoor.spendly.core.model.finance.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionAiEnrichmentRepositoryImpl @Inject constructor(
    private val dao: TransactionAiEnrichmentDao
) : TransactionAiEnrichmentRepository {

    override suspend fun upsert(entry: TransactionAiEnrichment) {
        dao.insert(entry.toEntity())
    }

    override suspend fun upsertPending(
        transactionType: TransactionType,
        transactionId: Long,
        promptVersion: Int
    ) {
        val now = System.currentTimeMillis()
        val existing = dao.getByTransaction(transactionType.name, transactionId)
        val entity = if (existing == null) {
            TransactionAiEnrichmentEntity(
                transactionType = transactionType.name,
                transactionId = transactionId,
                status = AiEnrichmentStatus.PENDING.name,
                counterpartyRole = CounterpartyRole.UNKNOWN.name,
                counterpartyType = CounterpartyType.UNKNOWN.name,
                identifierType = CounterpartyIdentifierType.NONE.name,
                paymentRail = PaymentRail.UNKNOWN.name,
                promptVersion = promptVersion,
                createdAt = now,
                modifiedAt = now
            )
        } else {
            if (existing.status == AiEnrichmentStatus.ENRICHED.name) {
                return
            }
            existing.copy(
                status = AiEnrichmentStatus.PENDING.name,
                promptVersion = promptVersion,
                modifiedAt = now
            )
        }
        dao.insert(entity)
    }

    override suspend fun upsertAll(entries: List<TransactionAiEnrichment>) {
        if (entries.isEmpty()) return
        dao.upsertAll(entries.map { it.toEntity() })
    }

    override suspend fun applyUpdates(updates: List<TransactionAiEnrichmentUpdate>) {
        if (updates.isEmpty()) return
        val now = System.currentTimeMillis()

        val entities = mutableListOf<TransactionAiEnrichmentEntity>()
        updates.forEach { update ->
            val existing = dao.getByTransaction(update.transactionType.name, update.transactionId)
            val entity = if (existing == null) {
                TransactionAiEnrichmentEntity(
                    transactionType = update.transactionType.name,
                    transactionId = update.transactionId,
                    status = update.status.name,
                    displayDescription = update.displayDescription,
                    counterpartyName = update.counterpartyName,
                    counterpartyRole = update.counterpartyRole.name,
                    counterpartyType = update.counterpartyType.name,
                    identifierType = update.identifierType.name,
                    identifierValue = update.identifierValue,
                    paymentRail = update.paymentRail.name,
                    confidence = update.confidence,
                    reason = update.reason,
                    modelName = update.modelName,
                    promptVersion = update.promptVersion,
                    enrichedAt = update.enrichedAt,
                    createdAt = now,
                    modifiedAt = now
                )
            } else {
                existing.copy(
                    status = update.status.name,
                    displayDescription = update.displayDescription,
                    counterpartyName = update.counterpartyName,
                    counterpartyRole = update.counterpartyRole.name,
                    counterpartyType = update.counterpartyType.name,
                    identifierType = update.identifierType.name,
                    identifierValue = update.identifierValue,
                    paymentRail = update.paymentRail.name,
                    confidence = update.confidence,
                    reason = update.reason,
                    modelName = update.modelName,
                    promptVersion = update.promptVersion,
                    enrichedAt = update.enrichedAt,
                    modifiedAt = now
                )
            }
            entities += entity
        }

        dao.upsertAll(entities)
    }

    override suspend fun getByTransaction(
        transactionType: TransactionType,
        transactionId: Long
    ): TransactionAiEnrichment? {
        return dao.getByTransaction(transactionType.name, transactionId)?.toDomain()
    }

    override fun observeByTransaction(
        transactionType: TransactionType,
        transactionId: Long
    ): Flow<TransactionAiEnrichment?> {
        return dao.observeByTransaction(transactionType.name, transactionId)
            .map { it?.toDomain() }
    }

    override suspend fun getByStatuses(
        statuses: Set<AiEnrichmentStatus>,
        limit: Int
    ): List<TransactionAiEnrichment> {
        if (statuses.isEmpty() || limit <= 0) return emptyList()
        return dao.getByStatuses(statuses.map { it.name }, limit)
            .map { it.toDomain() }
    }

    override suspend fun getByStatusesForMixedTransactions(
        statuses: Set<AiEnrichmentStatus>,
        expenseIds: List<Long>,
        incomeIds: List<Long>,
        limit: Int
    ): List<TransactionAiEnrichment> {
        if (statuses.isEmpty() || limit <= 0) return emptyList()
        if (expenseIds.isEmpty() && incomeIds.isEmpty()) return emptyList()

        val safeExpenseIds = if (expenseIds.isEmpty()) listOf(-1L) else expenseIds
        val safeIncomeIds = if (incomeIds.isEmpty()) listOf(-1L) else incomeIds

        return dao.getByStatusesForMixedTransactions(
            statuses = statuses.map { it.name },
            expenseIds = safeExpenseIds,
            incomeIds = safeIncomeIds,
            limit = limit
        ).map { it.toDomain() }
    }

    override fun observeAll(): Flow<List<TransactionAiEnrichment>> {
        return dao.observeAll().map { list -> list.map { it.toDomain() } }
    }

    private fun TransactionAiEnrichment.toEntity(): TransactionAiEnrichmentEntity {
        return TransactionAiEnrichmentEntity(
            id = id,
            transactionType = transactionType.name,
            transactionId = transactionId,
            status = status.name,
            displayDescription = displayDescription,
            counterpartyName = counterpartyName,
            counterpartyRole = counterpartyRole.name,
            counterpartyType = counterpartyType.name,
            identifierType = identifierType.name,
            identifierValue = identifierValue,
            paymentRail = paymentRail.name,
            confidence = confidence,
            reason = reason,
            modelName = modelName,
            promptVersion = promptVersion,
            enrichedAt = enrichedAt,
            createdAt = createdAt,
            modifiedAt = modifiedAt
        )
    }

    private fun TransactionAiEnrichmentEntity.toDomain(): TransactionAiEnrichment {
        return TransactionAiEnrichment(
            id = id,
            transactionType = parseEnum(transactionType, TransactionType.entries, TransactionType.EXPENSE),
            transactionId = transactionId,
            status = parseEnum(status, AiEnrichmentStatus.entries, AiEnrichmentStatus.PENDING),
            displayDescription = displayDescription,
            counterpartyName = counterpartyName,
            counterpartyRole = parseEnum(counterpartyRole, CounterpartyRole.entries, CounterpartyRole.UNKNOWN),
            counterpartyType = parseEnum(counterpartyType, CounterpartyType.entries, CounterpartyType.UNKNOWN),
            identifierType = parseEnum(identifierType, CounterpartyIdentifierType.entries, CounterpartyIdentifierType.NONE),
            identifierValue = identifierValue,
            paymentRail = parseEnum(paymentRail, PaymentRail.entries, PaymentRail.UNKNOWN),
            confidence = confidence,
            reason = reason,
            modelName = modelName,
            promptVersion = promptVersion,
            enrichedAt = enrichedAt,
            createdAt = createdAt,
            modifiedAt = modifiedAt
        )
    }

    private fun <T : Enum<T>> parseEnum(raw: String, values: List<T>, default: T): T {
        return values.firstOrNull { it.name == raw } ?: default
    }
}
