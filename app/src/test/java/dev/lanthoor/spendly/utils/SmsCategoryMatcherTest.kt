package dev.lanthoor.spendly.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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
}
