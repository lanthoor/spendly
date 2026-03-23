package dev.lanthoor.spendly.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SmsCategoryMatcherTest {

    private val now = System.currentTimeMillis()

    @Test
    fun `matches expense merchant SWIGGY to Food`() {
        val parsed = parsedExpense(merchant = "SWIGGY", description = "UPI payment")

        val result = SmsCategoryMatcher.match(parsed, "Paid to SWIGGY", "HDFCBK")

        assertNotNull(result)
        assertEquals("Food", result!!.categoryName)
    }

    @Test
    fun `matches expense merchant Amazon to Shopping`() {
        val parsed = parsedExpense(merchant = "Amazon", description = "Card spend")

        val result = SmsCategoryMatcher.match(parsed, "Card charged at Amazon India", "ICICIB")

        assertNotNull(result)
        assertEquals("Shopping", result!!.categoryName)
    }

    @Test
    fun `matches expense IRCTC to Travel`() {
        val parsed = parsedExpense(merchant = "IRCTC", description = "Ticket booking")

        val result = SmsCategoryMatcher.match(parsed, "IRCTC booking successful", "SBISMS")

        assertNotNull(result)
        assertEquals("Travel", result!!.categoryName)
    }

    @Test
    fun `matches income salary credited to Salary`() {
        val parsed = parsedIncome(description = "salary credited")

        val result = SmsCategoryMatcher.match(parsed, "salary credited by employer", "AXISBK")

        assertNotNull(result)
        assertEquals("Salary", result!!.categoryName)
    }

    @Test
    fun `matches income refund initiated to Refund`() {
        val parsed = parsedIncome(description = "refund initiated")

        val result = SmsCategoryMatcher.match(parsed, "refund initiated for failed txn", "KOTAK")

        assertNotNull(result)
        assertEquals("Refund", result!!.categoryName)
    }

    @Test
    fun `matches income interest credited to Interest`() {
        val parsed = parsedIncome(description = "interest credited")

        val result = SmsCategoryMatcher.match(parsed, "interest credited to your account", "HDFCBK")

        assertNotNull(result)
        assertEquals("Interest", result!!.categoryName)
    }

    @Test
    fun `merchant signal wins over broad body keywords`() {
        val parsed = parsedExpense(merchant = "Amazon", description = "purchase")

        val result = SmsCategoryMatcher.match(
            parsed,
            "fuel surcharge and petrol mention in footer",
            "ICICIB"
        )

        assertNotNull(result)
        assertEquals("Shopping", result!!.categoryName)
    }

    @Test
    fun `merchant hits tie breaker prefers category with more merchant matches`() {
        val parsed = parsedExpense(
            merchant = "SWIGGY",
            description = "dining amazon flipkart"
        )

        val result = SmsCategoryMatcher.match(
            parsed,
            "Paid via card using myntra gateway",
            "HDFCBK"
        )

        assertNotNull(result)
        assertEquals("Food", result!!.categoryName)
    }

    @Test
    fun `equal score and merchant hits returns null for ambiguous text`() {
        val parsed = parsedExpense(
            merchant = "SWIGGY AMAZON",
            description = "purchase"
        )

        val result = SmsCategoryMatcher.match(
            parsed,
            "Paid at SWIGGY and AMAZON",
            "ICICIB"
        )

        assertNull(result)
    }

    @Test
    fun `resolveCategory falls back to Others when matched name missing in lookup`() {
        val parsed = parsedIncome(description = "salary credited")
        val categoryLookup = SmsCategoryMatcher.buildCategoryLookup(
            listOf(
                category(id = 13L, name = "Others")
            )
        )

        val resolution = SmsCategoryMatcher.resolveCategory(
            parsed = parsed,
            smsBody = "salary credited by employer",
            sender = "HDFCBK",
            categoryLookup = categoryLookup
        )

        assertNotNull(resolution.category)
        assertEquals("Others", resolution.category!!.name)
        assertTrue(resolution.usedFallback)
        assertTrue(resolution.reason.startsWith("fallback:missing:salary"))
    }

    @Test
    fun `resolveCategory reports missing others when no fallback category exists`() {
        val parsed = parsedExpense(merchant = null, description = "bank transaction")
        val categoryLookup = SmsCategoryMatcher.buildCategoryLookup(emptyList())

        val resolution = SmsCategoryMatcher.resolveCategory(
            parsed = parsed,
            smsBody = "txn completed",
            sender = "HDFCBK",
            categoryLookup = categoryLookup
        )

        assertNull(resolution.category)
        assertTrue(resolution.usedFallback)
        assertEquals("fallback:missing:others", resolution.reason)
    }

    @Test
    fun `returns null for unknown text so workers can fallback to Others`() {
        val parsed = parsedExpense(merchant = null, description = "bank transaction")

        val result = SmsCategoryMatcher.match(parsed, "txn completed", "HDFCBK")

        assertNull(result)
    }

    private fun parsedExpense(merchant: String?, description: String): ParsedTransaction {
        return ParsedTransaction(
            amount = 10000L,
            transactionType = TransactionType.EXPENSE,
            date = now,
            description = description,
            accountHint = "1234",
            merchantName = merchant,
            confidence = 0.9f
        )
    }

    private fun parsedIncome(description: String): ParsedTransaction {
        return ParsedTransaction(
            amount = 10000L,
            transactionType = TransactionType.INCOME,
            date = now,
            description = description,
            accountHint = "5678",
            merchantName = null,
            confidence = 0.9f
        )
    }

    private fun category(id: Long, name: String): dev.lanthoor.spendly.domain.model.Category {
        return dev.lanthoor.spendly.domain.model.Category(
            id = id,
            name = name,
            icon = "category",
            color = 0,
            isCustom = false,
            sortOrder = 0
        )
    }
}
