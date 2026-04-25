package dev.lanthoor.spendly.domain.model.ai

import dev.lanthoor.spendly.core.model.finance.TransactionType

data class TransactionEnrichmentCandidate(
    val transactionType: TransactionType,
    val transactionId: Long,
    val amount: Long,
    val regexDescription: String,
    val smsBody: String,
    val smsSender: String,
    val smsTimestamp: Long
) {
    val txKey: String
        get() = "${transactionType.name}:$transactionId"
}
