package dev.lanthoor.spendly.utils.ai

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AiPromptBatchRequest(
    @SerialName("schema_version")
    val schemaVersion: Int,
    @SerialName("batch_id")
    val batchId: String,
    val transactions: List<AiPromptTransactionInput>
)

@Serializable
data class AiPromptTransactionInput(
    @SerialName("tx_key")
    val txKey: String,
    @SerialName("transaction_type")
    val transactionType: String,
    @SerialName("amount_paise")
    val amountPaise: Long,
    @SerialName("sms_sender")
    val smsSender: String,
    @SerialName("sms_timestamp")
    val smsTimestamp: Long,
    @SerialName("regex_description")
    val regexDescription: String,
    @SerialName("sms_body")
    val smsBody: String
)

@Serializable
data class AiPromptBatchResponse(
    @SerialName("schema_version")
    val schemaVersion: Int,
    val results: List<AiPromptTransactionResult>
)

@Serializable
data class AiPromptTransactionResult(
    @SerialName("tx_key")
    val txKey: String,
    val status: String,
    @SerialName("display_description")
    val displayDescription: String? = null,
    @SerialName("counterparty_name")
    val counterpartyName: String? = null,
    @SerialName("counterparty_role")
    val counterpartyRole: String,
    @SerialName("counterparty_type")
    val counterpartyType: String,
    @SerialName("identifier_type")
    val identifierType: String,
    @SerialName("identifier_value")
    val identifierValue: String? = null,
    @SerialName("payment_rail")
    val paymentRail: String,
    val confidence: Float? = null,
    val reason: String? = null
)
