package dev.lanthoor.spendly.ui.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Visual transformation that applies Indian number formatting (commas) to amount text fields.
 *
 * Formats numbers according to Indian numbering system:
 * - 1-3 digits: no comma (e.g., "123")
 * - 4 digits: comma after 1st digit (e.g., "1,234")
 * - 5 digits: comma after 2nd digit (e.g., "12,345")
 * - 6+ digits: comma after 3rd digit, then every 2 digits (e.g., "1,23,456", "12,34,567")
 *
 * Handles decimal points correctly - only formats the integer part:
 * - "123456.78" → "1,23,456.78"
 *
 * The transformation is display-only. The underlying state remains clean (no commas),
 * which ensures compatibility with existing validation and parsing logic.
 *
 * Usage:
 * ```
 * OutlinedTextField(
 *     value = amount,
 *     onValueChange = { /* ... */ },
 *     visualTransformation = IndianNumberFormatTransformation()
 * )
 * ```
 */
class IndianNumberFormatTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text

        if (originalText.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        // Split on decimal point
        val parts = originalText.split(".")

        val formatted = when {
            parts.size == 1 -> {
                // No decimal point - format entire string
                formatIndianNumber(parts[0])
            }
            parts.size == 2 -> {
                // Has decimal point - format only integer part
                val integerPart = formatIndianNumber(parts[0])
                val decimalPart = parts[1]
                "$integerPart.$decimalPart"
            }
            else -> {
                // Invalid format (multiple decimal points) - return as-is
                originalText
            }
        }

        return TransformedText(
            AnnotatedString(formatted),
            IndianNumberOffsetMapping(originalText, formatted)
        )
    }
}

/**
 * Format a number string with Indian comma placement.
 *
 * Indian numbering system places commas:
 * - First comma after 3 digits from right
 * - Subsequent commas every 2 digits
 *
 * Examples:
 * - "123" → "123" (no comma)
 * - "1234" → "1,234"
 * - "12345" → "12,345"
 * - "123456" → "1,23,456"
 * - "1234567" → "12,34,567"
 * - "12345678" → "1,23,45,678"
 *
 * @param number Number string to format (should not contain decimal point)
 * @return Formatted string with Indian comma placement
 */
private fun formatIndianNumber(number: String): String {
    if (number.isEmpty() || number.length <= 3) {
        return number
    }

    val result = StringBuilder()
    var remaining = number.length

    // Last 3 digits (rightmost group)
    val start = number.length - 3
    result.insert(0, number.substring(start))
    remaining -= 3

    // Groups of 2 digits from right to left
    var position = start
    while (remaining > 0) {
        result.insert(0, ',')
        val end = position
        position = maxOf(0, position - 2)
        result.insert(0, number.substring(position, end))
        remaining -= (end - position)
    }

    return result.toString()
}

/**
 * Offset mapping for Indian number formatting.
 *
 * Maps cursor positions between the original text (without commas) and the
 * transformed text (with commas). This ensures the cursor stays in the correct
 * position as commas appear and disappear during editing.
 *
 * @param original Original text without formatting
 * @param transformed Transformed text with Indian comma formatting
 */
private class IndianNumberOffsetMapping(
    private val original: String,
    private val transformed: String
) : OffsetMapping {

    /**
     * Maps position in original text to position in transformed text.
     *
     * Counts how many commas appear before this position in the transformed text
     * and adds them to the offset.
     *
     * Example: "123456" → "1,23,456"
     * - originalToTransformed(0) = 0 (before "1")
     * - originalToTransformed(1) = 2 (after "1,")
     * - originalToTransformed(3) = 5 (after "1,23,")
     * - originalToTransformed(6) = 8 (end)
     */
    override fun originalToTransformed(offset: Int): Int {
        var originalCount = 0
        var transformedPosition = 0

        // Count through `offset` non-comma characters
        while (transformedPosition < transformed.length && originalCount < offset) {
            if (transformed[transformedPosition] != ',') {
                originalCount++
            }
            transformedPosition++
        }

        // Skip any trailing commas to position cursor after them
        while (transformedPosition < transformed.length && transformed[transformedPosition] == ',') {
            transformedPosition++
        }

        return transformedPosition
    }

    /**
     * Maps position in transformed text to position in original text.
     *
     * Counts non-comma characters before this position to find the corresponding
     * position in the original text.
     *
     * Example: "1,23,456" → "123456"
     * - transformedToOriginal(0) = 0 (before "1")
     * - transformedToOriginal(1) = 1 (comma, maps to after "1")
     * - transformedToOriginal(2) = 1 (after "1,")
     * - transformedToOriginal(5) = 3 (after "1,23,")
     * - transformedToOriginal(9) = 6 (end)
     */
    override fun transformedToOriginal(offset: Int): Int {
        var originalPosition = 0

        for (i in 0 until offset.coerceAtMost(transformed.length)) {
            if (transformed[i] != ',') {
                originalPosition++
            }
        }

        return originalPosition.coerceAtMost(original.length)
    }
}
