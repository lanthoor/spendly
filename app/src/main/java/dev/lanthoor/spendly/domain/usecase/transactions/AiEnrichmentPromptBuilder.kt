package dev.lanthoor.spendly.domain.usecase.transactions

import dev.lanthoor.spendly.domain.model.ai.AiPromptBatchRequest
import dev.lanthoor.spendly.domain.model.ai.AiPromptTransactionInput
import dev.lanthoor.spendly.domain.model.ai.TransactionEnrichmentCandidate
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object AiEnrichmentPromptBuilder {
    const val SCHEMA_VERSION = 1

    private val json = Json {
        prettyPrint = false
        encodeDefaults = true
    }

    fun buildPrompt(batchId: String, candidates: List<TransactionEnrichmentCandidate>): String {
        val payload = AiPromptBatchRequest(
            schemaVersion = SCHEMA_VERSION,
            batchId = batchId,
            transactions = candidates.map {
                AiPromptTransactionInput(
                    txKey = it.txKey,
                    transactionType = it.transactionType.name,
                    amountPaise = it.amount,
                    smsSender = it.smsSender,
                    smsTimestamp = it.smsTimestamp,
                    regexDescription = it.regexDescription,
                    smsBody = it.smsBody
                )
            }
        )

        return """
You extract transaction counterparty metadata from Indian banking SMS.
Return ONLY valid JSON.
No markdown.
No explanation.
Do not invent details.
If unclear, use UNKNOWN or NONE and low confidence.

Rules:
- EXPENSE usually means counterparty is receiver/merchant.
- INCOME usually means counterparty is sender/remitter.
- Set payment_rail from SMS clues: UPI, IMPS, NEFT, RTGS, CARD, SI, ATM, UNKNOWN.
- Fill identifier fields only when explicit in SMS.

Output schema:
{
  "schema_version": 1,
  "results": [
    {
      "tx_key": "<string>",
      "status": "ENRICHED|UNCERTAIN",
      "display_description": "<string|null>",
      "counterparty_name": "<string|null>",
      "counterparty_role": "MERCHANT|SENDER|RECEIVER|BENEFICIARY|UNKNOWN",
      "counterparty_type": "PERSON|BUSINESS|BANK|UNKNOWN",
      "identifier_type": "VPA|ACCOUNT_LAST4|CARD_LAST4|UPI_REF|UTR|NONE",
      "identifier_value": "<string|null>",
      "payment_rail": "UPI|IMPS|NEFT|RTGS|CARD|SI|ATM|UNKNOWN",
      "confidence": <0..1|null>,
      "reason": "<string|null>"
    }
  ]
}

Input JSON:
${json.encodeToString(payload)}
        """.trimIndent()
    }
}
