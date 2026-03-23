package dev.lanthoor.spendly.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SmsDuplicateDetectorTest {

    @Test
    fun `same sender body timestamp is strict duplicate`() {
        val detector = SmsDuplicateDetector()
        val timestamp = 1_700_000_000_000L
        val parsed = parsed(10000L, TransactionType.EXPENSE, "1234", "amazon")

        val first = SmsFingerprintFactory.create("HDFCBK", "Rs 100 debited", timestamp, parsed)
        val second = SmsFingerprintFactory.create("HDFCBK", "Rs 100 debited", timestamp, parsed)

        detector.markSeen(first)

        assertEquals("strict", detector.findDuplicateReason(second))
    }

    @Test
    fun `same sender body with tiny timestamp drift is strict duplicate`() {
        val detector = SmsDuplicateDetector()
        val firstTime = 1_700_000_000_000L
        val secondTime = firstTime + 2 * 60 * 1000L
        val parsed = parsed(10000L, TransactionType.EXPENSE, "1234", "amazon")

        val first = SmsFingerprintFactory.create("AXISBK", "Rs 100 debited", firstTime, parsed)
        val second = SmsFingerprintFactory.create("AXISBK", "Rs 100 debited", secondTime, parsed)

        detector.markSeen(first)

        assertEquals("strict", detector.findDuplicateReason(second))
    }

    @Test
    fun `same amount type but different merchant is not duplicate`() {
        val detector = SmsDuplicateDetector()
        val timestamp = 1_700_000_000_000L
        val parsedA = parsed(10000L, TransactionType.EXPENSE, "1234", "amazon")
        val parsedB = parsed(10000L, TransactionType.EXPENSE, "1234", "flipkart")

        val first = SmsFingerprintFactory.create("HDFCBK", "Rs 100 debited at AMAZON", timestamp, parsedA)
        val second = SmsFingerprintFactory.create("HDFCBK", "Rs 100 debited at FLIPKART", timestamp, parsedB)

        detector.markSeen(first)

        assertNull(detector.findDuplicateReason(second))
    }

    @Test
    fun `same semantic fields in window is semantic duplicate`() {
        val detector = SmsDuplicateDetector()
        val firstTime = 1_700_000_000_000L
        val secondTime = firstTime + 3 * 60 * 1000L
        val firstParsed = parsed(25000L, TransactionType.EXPENSE, "1234", "zomato")
        val secondParsed = parsed(25000L, TransactionType.EXPENSE, "1234", "zomato")

        val first = SmsFingerprintFactory.create("ICICIB", "UPI txn id 123", firstTime, firstParsed)
        val second = SmsFingerprintFactory.create("ICICIB", "UPI txn id 456", secondTime, secondParsed)

        detector.markSeen(first)

        assertEquals("semantic", detector.findDuplicateReason(second))
    }

    @Test
    fun `same semantic fields with different sender is not duplicate`() {
        val detector = SmsDuplicateDetector()
        val firstTime = 1_700_000_000_000L
        val secondTime = firstTime + 3 * 60 * 1000L
        val firstParsed = parsed(25000L, TransactionType.EXPENSE, "1234", "zomato")
        val secondParsed = parsed(25000L, TransactionType.EXPENSE, "1234", "zomato")

        val first = SmsFingerprintFactory.create("ICICIB", "UPI txn id 123", firstTime, firstParsed)
        val second = SmsFingerprintFactory.create("HDFCBK", "UPI txn id 456", secondTime, secondParsed)

        detector.markSeen(first)

        assertNull(detector.findDuplicateReason(second))
    }

    @Test
    fun `same merchant and amount far apart in time is not duplicate`() {
        val detector = SmsDuplicateDetector()
        val firstTime = 1_700_000_000_000L
        val secondTime = firstTime + 45 * 60 * 1000L
        val parsed = parsed(20000L, TransactionType.EXPENSE, "1234", "swiggy")

        val first = SmsFingerprintFactory.create("ICICIB", "Rs 200 debited", firstTime, parsed)
        val second = SmsFingerprintFactory.create("ICICIB", "Rs 200 debited", secondTime, parsed)

        detector.markSeen(first)

        assertNull(detector.findDuplicateReason(second))
    }

    @Test
    fun `body casing and spacing variations still dedup`() {
        val detector = SmsDuplicateDetector()
        val timestamp = 1_700_000_000_000L
        val parsed = parsed(10000L, TransactionType.EXPENSE, "1234", "amazon")

        val first = SmsFingerprintFactory.create("HDFCBK", "Rs 100    debited", timestamp, parsed)
        val second = SmsFingerprintFactory.create("hdfcbk", "  rs 100 debited  ", timestamp, parsed)

        detector.markSeen(first)

        assertEquals("strict", detector.findDuplicateReason(second))
    }

    private fun parsed(
        amount: Long,
        type: TransactionType,
        accountHint: String?,
        merchant: String?
    ): ParsedTransaction {
        return ParsedTransaction(
            amount = amount,
            transactionType = type,
            date = 1_700_000_000_000L,
            description = merchant ?: "Bank Transaction (SMS)",
            accountHint = accountHint,
            merchantName = merchant,
            confidence = 1.0f
        )
    }
}
