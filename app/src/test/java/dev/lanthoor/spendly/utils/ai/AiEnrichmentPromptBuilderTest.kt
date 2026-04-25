package dev.lanthoor.spendly.domain.usecase.transactions

import dev.lanthoor.spendly.core.model.finance.TransactionType
import dev.lanthoor.spendly.domain.model.ai.TransactionEnrichmentCandidate
import org.junit.Assert.assertTrue
import org.junit.Test

class AiEnrichmentPromptBuilderTest {
    @Test
    fun `buildPrompt contains tx keys and schema`() {
        val candidates = listOf(
            TransactionEnrichmentCandidate(
                transactionType = TransactionType.EXPENSE,
                transactionId = 10L,
                amount = 15000L,
                regexDescription = "UPI Payment",
                smsBody = "Rs 150 sent to merchant@upi",
                smsSender = "HDFCBK",
                smsTimestamp = 12345L
            ),
            TransactionEnrichmentCandidate(
                transactionType = TransactionType.INCOME,
                transactionId = 11L,
                amount = 25000L,
                regexDescription = "Bank Credit",
                smsBody = "Rs 250 credited from abc@upi",
                smsSender = "SBISMS",
                smsTimestamp = 12346L
            )
        )

        val prompt = AiEnrichmentPromptBuilder.buildPrompt(
            batchId = "b1",
            candidates = candidates,
            allowedCategories = listOf("Food", "Salary", "Others")
        )

        assertTrue(prompt.contains("\"schema_version\":1"))
        assertTrue(prompt.contains("EXPENSE:10"))
        assertTrue(prompt.contains("INCOME:11"))
        assertTrue(prompt.contains("\"transactions\""))
        assertTrue(prompt.contains("\"allowed_categories\""))
    }
}
