package dev.lanthoor.spendly.domain.model

import dev.lanthoor.spendly.core.model.finance.AiEnrichmentStatus
import dev.lanthoor.spendly.core.model.finance.CounterpartyIdentifierType
import dev.lanthoor.spendly.core.model.finance.CounterpartyRole
import dev.lanthoor.spendly.core.model.finance.CounterpartyType
import dev.lanthoor.spendly.core.model.finance.PaymentRail
import dev.lanthoor.spendly.core.model.finance.TransactionType

data class TransactionAiEnrichment(
    val id: Long = 0,
    val transactionType: TransactionType,
    val transactionId: Long,
    val status: AiEnrichmentStatus,
    val displayDescription: String?,
    val counterpartyName: String?,
    val counterpartyRole: CounterpartyRole,
    val counterpartyType: CounterpartyType,
    val identifierType: CounterpartyIdentifierType,
    val identifierValue: String?,
    val paymentRail: PaymentRail,
    val confidence: Float?,
    val reason: String?,
    val modelName: String?,
    val promptVersion: Int,
    val enrichedAt: Long?,
    val createdAt: Long,
    val modifiedAt: Long
)
