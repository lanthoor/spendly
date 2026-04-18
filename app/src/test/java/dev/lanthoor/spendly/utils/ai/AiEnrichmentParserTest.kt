package dev.lanthoor.spendly.utils.ai

import dev.lanthoor.spendly.core.model.finance.AiEnrichmentStatus
import dev.lanthoor.spendly.core.model.finance.CounterpartyIdentifierType
import dev.lanthoor.spendly.core.model.finance.CounterpartyRole
import dev.lanthoor.spendly.core.model.finance.PaymentRail
import dev.lanthoor.spendly.core.model.finance.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Test

class AiEnrichmentParserTest {
    @Test
    fun `toUpdate parses enums and confidence`() {
        val candidate = TransactionEnrichmentCandidate(
            transactionType = TransactionType.EXPENSE,
            transactionId = 1L,
            amount = 1000L,
            regexDescription = "Regex Desc",
            smsBody = "Paid to merchant@upi",
            smsSender = "HDFCBK",
            smsTimestamp = 123L
        )
        val result = AiPromptTransactionResult(
            txKey = candidate.txKey,
            status = "ENRICHED",
            displayDescription = "AI Desc",
            counterpartyName = "Merchant",
            counterpartyRole = "MERCHANT",
            counterpartyType = "BUSINESS",
            identifierType = "VPA",
            identifierValue = "merchant@upi",
            paymentRail = "UPI",
            confidence = 0.95f,
            reason = "ok"
        )

        val update = AiEnrichmentParser.toUpdate(candidate, result, 1, "nano", 1000L)

        assertEquals(AiEnrichmentStatus.ENRICHED, update.status)
        assertEquals("AI Desc", update.displayDescription)
        assertEquals(CounterpartyRole.MERCHANT, update.counterpartyRole)
        assertEquals(CounterpartyIdentifierType.VPA, update.identifierType)
        assertEquals(PaymentRail.UPI, update.paymentRail)
        assertEquals(0.95f, update.confidence)
    }

    @Test
    fun `toUpdate falls back on unknown values`() {
        val candidate = TransactionEnrichmentCandidate(
            transactionType = TransactionType.INCOME,
            transactionId = 2L,
            amount = 1000L,
            regexDescription = "Regex Income",
            smsBody = "Credited from unknown",
            smsSender = "SBISMS",
            smsTimestamp = 123L
        )
        val result = AiPromptTransactionResult(
            txKey = candidate.txKey,
            status = "UNCERTAIN",
            displayDescription = "",
            counterpartyName = null,
            counterpartyRole = "BAD_ROLE",
            counterpartyType = "BAD_TYPE",
            identifierType = "BAD_ID",
            identifierValue = null,
            paymentRail = "BAD_RAIL",
            confidence = 2f,
            reason = null
        )

        val update = AiEnrichmentParser.toUpdate(candidate, result, 1, null, 1000L)

        assertEquals(AiEnrichmentStatus.FAILED, update.status)
        assertEquals("Regex Income", update.displayDescription)
        assertEquals(CounterpartyRole.UNKNOWN, update.counterpartyRole)
        assertEquals(CounterpartyIdentifierType.NONE, update.identifierType)
        assertEquals(PaymentRail.UNKNOWN, update.paymentRail)
        assertEquals(1f, update.confidence)
    }
}
