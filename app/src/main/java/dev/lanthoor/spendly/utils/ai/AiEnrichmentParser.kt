package dev.lanthoor.spendly.utils.ai

import dev.lanthoor.spendly.core.model.finance.AiEnrichmentStatus
import dev.lanthoor.spendly.core.model.finance.CounterpartyIdentifierType
import dev.lanthoor.spendly.core.model.finance.CounterpartyRole
import dev.lanthoor.spendly.core.model.finance.CounterpartyType
import dev.lanthoor.spendly.core.model.finance.PaymentRail
import dev.lanthoor.spendly.domain.model.TransactionAiEnrichmentUpdate

object AiEnrichmentParser {
    fun toUpdate(
        candidate: TransactionEnrichmentCandidate,
        result: AiPromptTransactionResult,
        promptVersion: Int,
        modelName: String?,
        enrichedAt: Long
    ): TransactionAiEnrichmentUpdate {
        return TransactionAiEnrichmentUpdate(
            transactionType = candidate.transactionType,
            transactionId = candidate.transactionId,
            status = parseStatus(result.status),
            displayDescription = sanitize(result.displayDescription, candidate.regexDescription),
            counterpartyName = sanitizeNullable(result.counterpartyName),
            counterpartyRole = parseEnum(result.counterpartyRole, CounterpartyRole.entries, CounterpartyRole.UNKNOWN),
            counterpartyType = parseEnum(result.counterpartyType, CounterpartyType.entries, CounterpartyType.UNKNOWN),
            identifierType = parseEnum(result.identifierType, CounterpartyIdentifierType.entries, CounterpartyIdentifierType.NONE),
            identifierValue = sanitizeNullable(result.identifierValue),
            paymentRail = parseEnum(result.paymentRail, PaymentRail.entries, PaymentRail.UNKNOWN),
            confidence = result.confidence?.coerceIn(0f, 1f),
            reason = sanitizeNullable(result.reason),
            modelName = modelName,
            promptVersion = promptVersion,
            enrichedAt = enrichedAt
        )
    }

    fun failedUpdate(
        candidate: TransactionEnrichmentCandidate,
        promptVersion: Int,
        reason: String,
        modelName: String? = null
    ): TransactionAiEnrichmentUpdate {
        val now = System.currentTimeMillis()
        return TransactionAiEnrichmentUpdate(
            transactionType = candidate.transactionType,
            transactionId = candidate.transactionId,
            status = AiEnrichmentStatus.FAILED,
            displayDescription = candidate.regexDescription,
            counterpartyName = null,
            counterpartyRole = CounterpartyRole.UNKNOWN,
            counterpartyType = CounterpartyType.UNKNOWN,
            identifierType = CounterpartyIdentifierType.NONE,
            identifierValue = null,
            paymentRail = PaymentRail.UNKNOWN,
            confidence = null,
            reason = reason,
            modelName = modelName,
            promptVersion = promptVersion,
            enrichedAt = now
        )
    }

    private fun parseStatus(raw: String): AiEnrichmentStatus {
        return when (raw.trim().uppercase()) {
            "ENRICHED" -> AiEnrichmentStatus.ENRICHED
            "UNCERTAIN", "FAILED" -> AiEnrichmentStatus.FAILED
            else -> AiEnrichmentStatus.FAILED
        }
    }

    private fun sanitize(value: String?, fallback: String): String {
        val cleaned = value?.trim().orEmpty()
        return if (cleaned.isBlank()) fallback else cleaned
    }

    private fun sanitizeNullable(value: String?): String? {
        val cleaned = value?.trim().orEmpty()
        return cleaned.takeIf { it.isNotBlank() }
    }

    private fun <T : Enum<T>> parseEnum(raw: String, values: List<T>, default: T): T {
        val key = raw.trim().uppercase()
        return values.firstOrNull { it.name == key } ?: default
    }
}
