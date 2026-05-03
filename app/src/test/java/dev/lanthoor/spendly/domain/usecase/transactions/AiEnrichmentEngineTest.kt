package dev.lanthoor.spendly.domain.usecase.transactions

import dev.lanthoor.spendly.domain.model.TransactionAiEnrichmentUpdate
import dev.lanthoor.spendly.domain.model.ai.AiPromptBatchResponse
import dev.lanthoor.spendly.domain.model.ai.AiPromptTransactionResult
import dev.lanthoor.spendly.domain.model.ai.TransactionEnrichmentCandidate
import kotlinx.serialization.SerializationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AiEnrichmentEngineTest {

    private val engine = AiEnrichmentEngine()

    @Test
    fun `parseResponse handles valid json`() {
        val json = """{"schema_version":1,"results":[]}"""
        val response = engine.parseResponse(json)
        assertEquals(0, response.results.size)
    }

    @Test
    fun `parseResponse extracts json from markdown`() {
        val markdown = """
            Here is the result:
            ```json
            {"schema_version":1,"results":[]}
            ```
            Hope this helps!
        """.trimIndent()
        val response = engine.parseResponse(markdown)
        assertEquals(0, response.results.size)
    }

    @Test(expected = SerializationException::class)
    fun `parseResponse throws when no json found`() {
        engine.parseResponse("not json")
    }

    @Test
    fun `splitIntoBatches respects batch size`() {
        val candidates = (1..5).map {
            TransactionEnrichmentCandidate(
                transactionType = dev.lanthoor.spendly.core.model.finance.TransactionType.EXPENSE,
                transactionId = it.toLong(),
                amount = 100L,
                regexDescription = "desc",
                smsBody = "body",
                smsSender = "sender",
                smsTimestamp = 0L
            )
        }
        val batches = engine.splitIntoBatches(candidates, batchSize = 2)
        assertEquals(3, batches.size)
        assertEquals(2, batches[0].size)
        assertEquals(2, batches[1].size)
        assertEquals(1, batches[2].size)
    }

    @Test
    fun `splitIntoBatches respects character budget`() {
        val longBody = "a".repeat(4000)
        val candidates = (1..3).map {
            TransactionEnrichmentCandidate(
                transactionType = dev.lanthoor.spendly.core.model.finance.TransactionType.EXPENSE,
                transactionId = it.toLong(),
                amount = 100L,
                regexDescription = "desc",
                smsBody = longBody,
                smsSender = "sender",
                smsTimestamp = 0L
            )
        }
        // Max budget is 8000. 3 * 4000 > 8000. Should split.
        val batches = engine.splitIntoBatches(candidates, batchSize = 10, maxCharBudget = 8000)
        assertTrue(batches.size > 1)
    }

    @Test
    fun `buildUpdatesFromResponse maps results correctly`() {
        val candidates = listOf(
            TransactionEnrichmentCandidate(
                transactionType = dev.lanthoor.spendly.core.model.finance.TransactionType.EXPENSE,
                transactionId = 1L,
                amount = 100L,
                regexDescription = "desc",
                smsBody = "body",
                smsSender = "sender",
                smsTimestamp = 0L
            )
        )
        val response = AiPromptBatchResponse(
            schemaVersion = 1,
            results = listOf(
                AiPromptTransactionResult(
                    txKey = "EXPENSE:1",
                    status = "ENRICHED",
                    displayDescription = "Enriched Desc",
                    counterpartyName = "Merchant",
                    counterpartyRole = "MERCHANT",
                    counterpartyType = "BUSINESS",
                    identifierType = "NONE",
                    identifierValue = null,
                    paymentRail = "UPI",
                    categoryName = "Groceries",
                    confidence = 0.9f,
                    reason = "ok"
                )
            )
        )
        val categoryLookup = mapOf("groceries" to 10L)
        val updates = engine.buildUpdatesFromResponse(
            candidates = candidates,
            response = response,
            categoryLookup = categoryLookup,
            promptVersion = 1,
            modelName = "gpt-4",
            enrichedAt = 12345L,
            categoryResolver = { name, lookup -> lookup[name?.lowercase()] }
        )
        assertEquals(1, updates.size)
        assertEquals(10L, updates[0].categoryId)
        assertEquals("Enriched Desc", updates[0].displayDescription)
    }

    @Test
    fun `buildUpdatesFromResponse handles missing results`() {
        val candidates = listOf(
            TransactionEnrichmentCandidate(
                transactionType = dev.lanthoor.spendly.core.model.finance.TransactionType.EXPENSE,
                transactionId = 1L,
                amount = 100L,
                regexDescription = "desc",
                smsBody = "body",
                smsSender = "sender",
                smsTimestamp = 0L
            )
        )
        val response = AiPromptBatchResponse(schemaVersion = 1, results = emptyList())
        val updates = engine.buildUpdatesFromResponse(
            candidates = candidates,
            response = response,
            categoryLookup = emptyMap(),
            promptVersion = 1,
            modelName = "gpt-4",
            enrichedAt = 12345L,
            categoryResolver = { _, _ -> null }
        )
        assertEquals(1, updates.size)
        assertEquals(dev.lanthoor.spendly.core.model.finance.AiEnrichmentStatus.FAILED, updates[0].status)
        assertEquals("missing-result", updates[0].reason)
    }
}
