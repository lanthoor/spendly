package `in`.mylullaby.spendly.utils

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for CurrencyUtils with ZERO tolerance.
 *
 * All tests use exact equality - no precision loss allowed.
 * Tests currency conversion using integer-only arithmetic.
 */
class CurrencyUtilsTest {

    // ========== PARSING TESTS - ZERO TOLERANCE ==========

    @Test
    fun parseRupeesToPaise_basicAmounts_exactMatch() {
        assertEquals(100L, CurrencyUtils.parseRupeesToPaise("1.0"))
        assertEquals(100L, CurrencyUtils.parseRupeesToPaise("1.00"))
        assertEquals(12345L, CurrencyUtils.parseRupeesToPaise("123.45"))
        assertEquals(99999L, CurrencyUtils.parseRupeesToPaise("999.99"))
        assertEquals(0L, CurrencyUtils.parseRupeesToPaise("0"))
        assertEquals(0L, CurrencyUtils.parseRupeesToPaise("0.0"))
        assertEquals(0L, CurrencyUtils.parseRupeesToPaise("0.00"))
        assertEquals(50L, CurrencyUtils.parseRupeesToPaise("0.50"))
        assertEquals(5L, CurrencyUtils.parseRupeesToPaise("0.05"))
    }

    @Test
    fun parseRupeesToPaise_largeAmounts_exactMatch() {
        // NO TOLERANCE - must be EXACT (this was failing with Double arithmetic)
        assertEquals(10000000L, CurrencyUtils.parseRupeesToPaise("100000.0"))
        assertEquals(123456789L, CurrencyUtils.parseRupeesToPaise("1234567.89"))
        assertEquals(99999999L, CurrencyUtils.parseRupeesToPaise("999999.99"))
    }

    @Test
    fun parseRupeesToPaise_withRupeeSymbol_exactMatch() {
        assertEquals(100L, CurrencyUtils.parseRupeesToPaise("₹1.0"))
        assertEquals(12345L, CurrencyUtils.parseRupeesToPaise("₹123.45"))
        assertEquals(123456789L, CurrencyUtils.parseRupeesToPaise("₹1234567.89"))
    }

    @Test
    fun parseRupeesToPaise_withCommas_exactMatch() {
        assertEquals(100000L, CurrencyUtils.parseRupeesToPaise("1,000.0"))
        assertEquals(123456789L, CurrencyUtils.parseRupeesToPaise("₹1,234,567.89"))
        assertEquals(10000000L, CurrencyUtils.parseRupeesToPaise("100,000.00"))
        assertEquals(12345678900L, CurrencyUtils.parseRupeesToPaise("123,456,789.00"))
    }

    @Test
    fun parseRupeesToPaise_withWhitespace_exactMatch() {
        assertEquals(12345L, CurrencyUtils.parseRupeesToPaise("  123.45  "))
        assertEquals(12345L, CurrencyUtils.parseRupeesToPaise("₹ 123.45"))
        assertEquals(12345L, CurrencyUtils.parseRupeesToPaise(" ₹ 123.45 "))
    }

    @Test
    fun parseRupeesToPaise_noDecimal_exactMatch() {
        assertEquals(12300L, CurrencyUtils.parseRupeesToPaise("123"))
        assertEquals(100L, CurrencyUtils.parseRupeesToPaise("1"))
        assertEquals(100000000L, CurrencyUtils.parseRupeesToPaise("1000000"))
    }

    @Test
    fun parseRupeesToPaise_singleDecimalDigit_padsCorrectly() {
        assertEquals(50L, CurrencyUtils.parseRupeesToPaise("0.5")) // "5" → "50" paise
        assertEquals(1250L, CurrencyUtils.parseRupeesToPaise("12.5")) // "5" → "50" paise
    }

    @Test
    fun parseRupeesToPaise_moreThanTwoDecimals_truncates() {
        assertEquals(12345L, CurrencyUtils.parseRupeesToPaise("123.456")) // Truncate "456" → "45"
        assertEquals(12399L, CurrencyUtils.parseRupeesToPaise("123.999")) // Truncate "999" → "99"
    }

    // ========== FORMATTING TESTS - ZERO TOLERANCE ==========

    @Test
    fun formatPaise_basicAmounts_exactMatch() {
        assertEquals("₹1.00", CurrencyUtils.formatPaise(100))
        assertEquals("₹123.45", CurrencyUtils.formatPaise(12345))
        assertEquals("₹999.99", CurrencyUtils.formatPaise(99999))
        assertEquals("₹0.00", CurrencyUtils.formatPaise(0))
        assertEquals("₹0.50", CurrencyUtils.formatPaise(50))
        assertEquals("₹0.05", CurrencyUtils.formatPaise(5))
        assertEquals("₹0.01", CurrencyUtils.formatPaise(1))
    }

    @Test
    fun formatPaise_largeAmounts_exactMatch() {
        assertEquals("₹100000.00", CurrencyUtils.formatPaise(10000000))
        assertEquals("₹1234567.89", CurrencyUtils.formatPaise(123456789))
        assertEquals("₹999999.99", CurrencyUtils.formatPaise(99999999))
    }

    // ========== ROUND-TRIP TESTS - ZERO TOLERANCE ==========

    @Test
    fun roundTrip_parseAndFormat_exactMatch() {
        val testCases = listOf(
            "123.45" to 12345L,
            "1234567.89" to 123456789L,
            "0.99" to 99L,
            "0.01" to 1L,
            "999999.99" to 99999999L,
            "1.00" to 100L,
            "100000.00" to 10000000L
        )

        testCases.forEach { (input, expectedPaise) ->
            // Parse
            val parsed = CurrencyUtils.parseRupeesToPaise(input)
            assertEquals("Parse $input", expectedPaise, parsed)

            // Format
            val formatted = CurrencyUtils.formatPaise(parsed)
            // Parse formatted string back
            val reparsed = CurrencyUtils.parseRupeesToPaise(formatted)
            assertEquals("Round-trip $input", expectedPaise, reparsed)
        }
    }

    @Test
    fun roundTrip_allPaiseValues_0to999_exactMatch() {
        // Test EVERY paise value from 0 to 999 paise - ALL must be exact
        for (paise in 0L..999L) {
            val formatted = CurrencyUtils.formatPaise(paise)
            val parsed = CurrencyUtils.parseRupeesToPaise(formatted)
            assertEquals("Round-trip for $paise paise", paise, parsed)
        }
    }

    @Test
    fun roundTrip_largeRandomValues_exactMatch() {
        val testValues = listOf(
            1L, 99L, 100L, 999L, 1000L,
            12345L, 99999L, 100000L,
            123456789L, 999999999L,
            Long.MAX_VALUE / 100 // Largest valid paise value
        )

        testValues.forEach { paise ->
            val formatted = CurrencyUtils.formatPaise(paise)
            val parsed = CurrencyUtils.parseRupeesToPaise(formatted)
            assertEquals("Round-trip for $paise paise", paise, parsed)
        }
    }

    // ========== VALIDATION TESTS ==========

    @Test
    fun isValidRupeeString_validInputs_returnsTrue() {
        assertTrue(CurrencyUtils.isValidRupeeString("123.45"))
        assertTrue(CurrencyUtils.isValidRupeeString("₹123.45"))
        assertTrue(CurrencyUtils.isValidRupeeString("1,234.56"))
        assertTrue(CurrencyUtils.isValidRupeeString("0.01"))
        assertTrue(CurrencyUtils.isValidRupeeString("999999.99"))
        assertTrue(CurrencyUtils.isValidRupeeString("123"))
        assertTrue(CurrencyUtils.isValidRupeeString("0"))
    }

    @Test
    fun isValidRupeeString_invalidInputs_returnsFalse() {
        assertFalse(CurrencyUtils.isValidRupeeString("abc"))
        assertFalse(CurrencyUtils.isValidRupeeString(""))
        assertFalse(CurrencyUtils.isValidRupeeString("12.34.56"))
        assertFalse(CurrencyUtils.isValidRupeeString("₹"))
        assertFalse(CurrencyUtils.isValidRupeeString("   "))
    }

    // ========== paiseToRupees() - Display Only ==========
    // This method returns Double for display, but should still be accurate

    @Test
    fun paiseToRupees_convertsCorrectly() {
        assertEquals(1.0, CurrencyUtils.paiseToRupees(100), 0.0001)
        assertEquals(123.45, CurrencyUtils.paiseToRupees(12345), 0.0001)
        assertEquals(999.99, CurrencyUtils.paiseToRupees(99999), 0.0001)
        assertEquals(0.0, CurrencyUtils.paiseToRupees(0), 0.0001)
        assertEquals(1234567.89, CurrencyUtils.paiseToRupees(123456789), 0.0001)
    }
}
