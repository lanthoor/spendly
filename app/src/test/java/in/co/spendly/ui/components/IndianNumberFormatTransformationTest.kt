package `in`.co.spendly.ui.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for IndianNumberFormatTransformation.
 *
 * Tests:
 * - Formatting logic for various digit counts
 * - Decimal point handling
 * - Edge cases (empty, single digit, etc.)
 * - Cursor position mapping (offset mapping)
 */
class IndianNumberFormatTransformationTest {

    private val transformation = IndianNumberFormatTransformation()

    // ========== Formatting Tests ==========

    @Test
    fun `format empty string`() {
        val result = transformation.filter(AnnotatedString(""))
        assertEquals("", result.text.text)
    }

    @Test
    fun `format single digit`() {
        assertEquals("0", transform("0"))
        assertEquals("1", transform("1"))
        assertEquals("9", transform("9"))
    }

    @Test
    fun `format two digits`() {
        assertEquals("10", transform("10"))
        assertEquals("99", transform("99"))
    }

    @Test
    fun `format three digits - no comma`() {
        assertEquals("123", transform("123"))
        assertEquals("999", transform("999"))
    }

    @Test
    fun `format four digits - first comma appears`() {
        assertEquals("1,234", transform("1234"))
        assertEquals("9,999", transform("9999"))
    }

    @Test
    fun `format five digits`() {
        assertEquals("12,345", transform("12345"))
        assertEquals("99,999", transform("99999"))
    }

    @Test
    fun `format six digits - Indian pattern begins`() {
        assertEquals("1,23,456", transform("123456"))
        assertEquals("9,99,999", transform("999999"))
    }

    @Test
    fun `format seven digits`() {
        assertEquals("12,34,567", transform("1234567"))
        assertEquals("99,99,999", transform("9999999"))
    }

    @Test
    fun `format eight digits`() {
        assertEquals("1,23,45,678", transform("12345678"))
        assertEquals("9,99,99,999", transform("99999999"))
    }

    @Test
    fun `format nine digits`() {
        assertEquals("12,34,56,789", transform("123456789"))
    }

    @Test
    fun `format ten digits`() {
        assertEquals("1,23,45,67,890", transform("1234567890"))
    }

    // ========== Decimal Tests ==========

    @Test
    fun `format with decimal - under 4 digits`() {
        assertEquals("1.0", transform("1.0"))
        assertEquals("12.34", transform("12.34"))
        assertEquals("123.45", transform("123.45"))
    }

    @Test
    fun `format with decimal - 4 digits`() {
        assertEquals("1,234.56", transform("1234.56"))
    }

    @Test
    fun `format with decimal - 5 digits`() {
        assertEquals("12,345.67", transform("12345.67"))
    }

    @Test
    fun `format with decimal - 6 digits Indian pattern`() {
        assertEquals("1,23,456.78", transform("123456.78"))
    }

    @Test
    fun `format with decimal - 7+ digits`() {
        assertEquals("12,34,567.89", transform("1234567.89"))
        assertEquals("1,23,45,678.90", transform("12345678.90"))
    }

    @Test
    fun `format with trailing decimal point`() {
        assertEquals("123.", transform("123."))
        assertEquals("1,234.", transform("1234."))
    }

    @Test
    fun `format with leading decimal point`() {
        assertEquals(".5", transform(".5"))
        assertEquals(".50", transform(".50"))
    }

    @Test
    fun `format with single decimal digit`() {
        assertEquals("123.4", transform("123.4"))
        assertEquals("1,234.5", transform("1234.5"))
    }

    @Test
    fun `format with two decimal digits`() {
        assertEquals("123.45", transform("123.45"))
        assertEquals("1,234.56", transform("1234.56"))
    }

    // ========== Edge Cases ==========

    @Test
    fun `format zero`() {
        assertEquals("0", transform("0"))
    }

    @Test
    fun `format zero with decimal`() {
        assertEquals("0.0", transform("0.0"))
        assertEquals("0.00", transform("0.00"))
    }

    @Test
    fun `format with invalid multiple decimals - returns as-is`() {
        // Invalid format should return original
        assertEquals("12.34.56", transform("12.34.56"))
    }

    // ========== Offset Mapping Tests ==========

    @Test
    fun `offset mapping for empty string`() {
        val result = transformation.filter(AnnotatedString(""))
        val mapping = result.offsetMapping

        assertEquals(0, mapping.originalToTransformed(0))
        assertEquals(0, mapping.transformedToOriginal(0))
    }

    @Test
    fun `offset mapping for no commas`() {
        val result = transformation.filter(AnnotatedString("123"))
        val mapping = result.offsetMapping

        // Original positions map 1:1 when no commas
        assertEquals(0, mapping.originalToTransformed(0))
        assertEquals(1, mapping.originalToTransformed(1))
        assertEquals(2, mapping.originalToTransformed(2))
        assertEquals(3, mapping.originalToTransformed(3))

        // Reverse mapping
        assertEquals(0, mapping.transformedToOriginal(0))
        assertEquals(1, mapping.transformedToOriginal(1))
        assertEquals(2, mapping.transformedToOriginal(2))
        assertEquals(3, mapping.transformedToOriginal(3))
    }

    @Test
    fun `offset mapping for 4 digits - one comma`() {
        // "1234" → "1,234"
        val result = transformation.filter(AnnotatedString("1234"))
        val mapping = result.offsetMapping

        // Original to transformed (commas add offsets)
        assertEquals(0, mapping.originalToTransformed(0)) // Before "1"
        assertEquals(2, mapping.originalToTransformed(1)) // After "1," (comma inserted)
        assertEquals(3, mapping.originalToTransformed(2)) // After "1,2"
        assertEquals(4, mapping.originalToTransformed(3)) // After "1,23"
        assertEquals(5, mapping.originalToTransformed(4)) // After "1,234"

        // Transformed to original (skip commas)
        assertEquals(0, mapping.transformedToOriginal(0)) // Before "1"
        assertEquals(1, mapping.transformedToOriginal(1)) // After "1"
        assertEquals(1, mapping.transformedToOriginal(2)) // After "1," (comma is at position 1 in original)
        assertEquals(2, mapping.transformedToOriginal(3)) // After "1,2"
        assertEquals(3, mapping.transformedToOriginal(4)) // After "1,23"
        assertEquals(4, mapping.transformedToOriginal(5)) // After "1,234"
    }

    @Test
    fun `offset mapping for 6 digits - two commas`() {
        // "123456" → "1,23,456"
        val result = transformation.filter(AnnotatedString("123456"))
        val mapping = result.offsetMapping

        // Original to transformed
        assertEquals(0, mapping.originalToTransformed(0)) // Before "1"
        assertEquals(2, mapping.originalToTransformed(1)) // After "1," (first comma)
        assertEquals(3, mapping.originalToTransformed(2)) // After "1,2"
        assertEquals(5, mapping.originalToTransformed(3)) // After "1,23," (second comma)
        assertEquals(6, mapping.originalToTransformed(4)) // After "1,23,4"
        assertEquals(7, mapping.originalToTransformed(5)) // After "1,23,45"
        assertEquals(8, mapping.originalToTransformed(6)) // After "1,23,456"

        // Transformed to original
        assertEquals(0, mapping.transformedToOriginal(0)) // Before "1"
        assertEquals(1, mapping.transformedToOriginal(1)) // After "1"
        assertEquals(1, mapping.transformedToOriginal(2)) // After "1," (comma)
        assertEquals(2, mapping.transformedToOriginal(3)) // After "1,2"
        assertEquals(3, mapping.transformedToOriginal(4)) // After "1,23"
        assertEquals(3, mapping.transformedToOriginal(5)) // After "1,23," (comma)
        assertEquals(4, mapping.transformedToOriginal(6)) // After "1,23,4"
        assertEquals(5, mapping.transformedToOriginal(7)) // After "1,23,45"
        assertEquals(6, mapping.transformedToOriginal(8)) // After "1,23,456"
    }

    @Test
    fun `offset mapping for decimal - commas only in integer part`() {
        // "123456.78" → "1,23,456.78"
        val result = transformation.filter(AnnotatedString("123456.78"))
        val mapping = result.offsetMapping

        // Original to transformed - before decimal
        assertEquals(0, mapping.originalToTransformed(0)) // Before "1"
        assertEquals(2, mapping.originalToTransformed(1)) // After "1,"
        assertEquals(5, mapping.originalToTransformed(3)) // After "1,23,"

        // After decimal - no commas added
        assertEquals(8, mapping.originalToTransformed(6)) // After "1,23,456" (before decimal)
        assertEquals(9, mapping.originalToTransformed(7)) // After "1,23,456." (decimal point)
        assertEquals(10, mapping.originalToTransformed(8)) // After "1,23,456.7"
        assertEquals(11, mapping.originalToTransformed(9)) // After "1,23,456.78"

        // Transformed to original
        assertEquals(0, mapping.transformedToOriginal(0)) // Before "1"
        assertEquals(1, mapping.transformedToOriginal(2)) // After "1,"
        assertEquals(3, mapping.transformedToOriginal(5)) // After "1,23,"
        assertEquals(6, mapping.transformedToOriginal(8)) // After "1,23,456" (before decimal)
        assertEquals(7, mapping.transformedToOriginal(9)) // After "1,23,456." (decimal point)
        assertEquals(8, mapping.transformedToOriginal(10)) // After "1,23,456.7"
        assertEquals(9, mapping.transformedToOriginal(11)) // After "1,23,456.78"
    }

    @Test
    fun `offset mapping at end of string`() {
        val result = transformation.filter(AnnotatedString("123456"))
        val mapping = result.offsetMapping

        // Test end positions don't overflow
        assertEquals(8, mapping.originalToTransformed(6))
        assertEquals(6, mapping.transformedToOriginal(8))

        // Test beyond end (should clamp)
        assertEquals(8, mapping.originalToTransformed(10))
        assertEquals(6, mapping.transformedToOriginal(20))
    }

    @Test
    fun `offset mapping for various positions in real-world case`() {
        // "50000" → "50,000" (common salary amount)
        val result = transformation.filter(AnnotatedString("50000"))
        val mapping = result.offsetMapping

        assertEquals(0, mapping.originalToTransformed(0)) // Before "5"
        assertEquals(1, mapping.originalToTransformed(1)) // After "5"
        assertEquals(3, mapping.originalToTransformed(2)) // After "50," (comma inserted)
        assertEquals(4, mapping.originalToTransformed(3)) // After "50,0"
        assertEquals(5, mapping.originalToTransformed(4)) // After "50,00"
        assertEquals(6, mapping.originalToTransformed(5)) // After "50,000"
    }

    // ========== Helper Functions ==========

    /**
     * Helper to transform a string and return the text result.
     */
    private fun transform(input: String): String {
        return transformation.filter(AnnotatedString(input)).text.text
    }
}
