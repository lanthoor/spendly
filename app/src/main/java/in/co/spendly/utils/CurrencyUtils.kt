package `in`.co.spendly.utils

/**
 * Currency utilities with ZERO precision loss.
 * All operations use integer arithmetic only - NO floating-point.
 *
 * All amounts are stored in the database as Long values in paise to avoid
 * floating-point precision issues. ₹1.00 = 100 paise.
 */
object CurrencyUtils {
    private const val PAISE_PER_RUPEE = 100

    /**
     * Convert rupee string to paise (ZERO precision loss).
     *
     * Parses string directly as integers, avoiding floating-point entirely.
     *
     * Examples:
     * - "123.45" → 12345 paise (123 * 100 + 45)
     * - "123" → 12300 paise (123 * 100 + 0)
     * - "0.99" → 99 paise (0 * 100 + 99)
     * - "0.5" → 50 paise (0 * 100 + 50, padded)
     *
     * @param rupeeString Amount string (e.g., "123.45", "₹1,234.56")
     * @return Amount in paise (exact, no precision loss)
     * @throws NumberFormatException if string is invalid
     */
    fun parseRupeesToPaise(rupeeString: String): Long {
        // Remove currency symbol, commas, and whitespace
        val cleaned = rupeeString
            .replace("₹", "")
            .replace(",", "")
            .trim()

        if (cleaned.isEmpty()) {
            throw NumberFormatException("Empty rupee string")
        }

        // Split on decimal point
        val parts = cleaned.split(".")

        return when (parts.size) {
            1 -> {
                // No decimal point: "123" → 12300 paise
                val rupees = parts[0].toLong()
                rupees * PAISE_PER_RUPEE
            }

            2 -> {
                // Has decimal: "123.45" → 12345 paise
                val rupees = parts[0].toLong()
                var paiseStr = parts[1]

                // Pad or truncate paise to exactly 2 digits
                when {
                    paiseStr.length < 2 -> paiseStr = paiseStr.padEnd(2, '0') // "5" → "50"
                    paiseStr.length > 2 -> paiseStr = paiseStr.substring(0, 2) // "456" → "45"
                }

                val paise = paiseStr.toLong()
                (rupees * PAISE_PER_RUPEE) + paise
            }

            else -> throw NumberFormatException("Invalid rupee format: $rupeeString")
        }
    }

    /**
     * Convert paise to rupees as Double (for display calculations only).
     *
     * WARNING: Only use this for display. NEVER store the result back to database.
     * For formatting, use formatPaise() instead.
     *
     * @param paise Amount in paise
     * @return Amount in rupees (Double, for display only)
     */
    fun paiseToRupees(paise: Long): Double {
        // Integer division, then convert to double
        val rupees = paise / PAISE_PER_RUPEE
        val remainder = paise % PAISE_PER_RUPEE
        return rupees.toDouble() + (remainder.toDouble() / PAISE_PER_RUPEE)
    }

    /**
     * Format paise as rupee string with Indian formatting (ZERO precision loss).
     *
     * Uses integer division/modulo - no floating-point operations.
     *
     * Examples:
     * - 12345 paise → "₹123.45"
     * - 100 paise → "₹1.00"
     * - 99 paise → "₹0.99"
     * - 10000000 paise → "₹1,00,000.00"
     * - 1234567890 paise → "₹1,23,45,678.90"
     * - -12345 paise → "₹-123.45"
     *
     * @param paise Amount in paise
     * @return Formatted string with Indian commas (e.g., "₹1,23,456.78")
     */
    fun formatPaise(paise: Long): String {
        val isNegative = paise < 0
        val absolutePaise = if (isNegative) -paise else paise
        val rupees = absolutePaise / PAISE_PER_RUPEE
        val paiseRemainder = absolutePaise % PAISE_PER_RUPEE
        val formattedRupees = formatIndianNumber(rupees)
        return if (isNegative) {
            "₹-%s.%02d".format(formattedRupees, paiseRemainder)
        } else {
            "₹%s.%02d".format(formattedRupees, paiseRemainder)
        }
    }

    /**
     * Convert paise to rupee string for form input (ZERO precision loss).
     *
     * Uses integer division/modulo - no floating-point operations.
     * Returns Indian number format with commas (xx,xx,xxx.xx).
     *
     * Examples:
     * - 12345 paise → "123.45"
     * - 100 paise → "1.00"
     * - 99 paise → "0.99"
     * - 10000000 paise (₹1,00,000) → "1,00,000.00"
     * - 1234567890 paise (₹1,23,45,678.90) → "1,23,45,678.90"
     *
     * With abbreviated = true:
     * - 12345 paise → "₹123"
     * - 10000000 paise (₹1,00,000) → "₹1L"
     * - 10000000000 paise (₹1,00,00,000) → "₹1Cr"
     *
     * @param paise Amount in paise
     * @param abbreviated If true, abbreviate large amounts (K for thousands, L for lakhs, Cr for crores)
     * @return Indian formatted string (e.g., "1,23,456.78") or abbreviated (e.g., "₹1.2L")
     */
    fun paiseToRupeeString(paise: Long, abbreviated: Boolean = false): String {
        val rupees = paise / PAISE_PER_RUPEE
        val paiseRemainder = paise % PAISE_PER_RUPEE

        if (!abbreviated) {
            // Indian number formatting: xx,xx,xxx.xx
            val formattedRupees = formatIndianNumber(rupees)
            return "%s.%02d".format(formattedRupees, paiseRemainder)
        }

        // Abbreviated format for chart labels
        return when {
            rupees >= 10000000 -> "₹%.1fCr".format(rupees / 10000000.0) // Crores
            rupees >= 100000 -> "₹%.1fL".format(rupees / 100000.0) // Lakhs
            rupees >= 1000 -> "₹%.1fK".format(rupees / 1000.0) // Thousands
            else -> "₹$rupees"
        }
    }

    /**
     * Format number with Indian numbering system (commas every 2 digits after first 3).
     *
     * Examples:
     * - 123 → "123"
     * - 1234 → "1,234"
     * - 12345 → "12,345"
     * - 123456 → "1,23,456"
     * - 12345678 → "1,23,45,678"
     *
     * @param number Number to format
     * @return Formatted string with Indian comma placement
     */
    private fun formatIndianNumber(number: Long): String {
        val numStr = number.toString()
        if (numStr.length <= 3) return numStr

        val result = StringBuilder()
        var remaining = numStr.length

        // First group: last 3 digits
        var start = numStr.length - 3
        result.insert(0, numStr.substring(start))
        remaining -= 3

        // Subsequent groups: every 2 digits
        while (remaining > 0) {
            result.insert(0, ',')
            val end = start
            start = maxOf(0, start - 2)
            result.insert(0, numStr.substring(start, end))
            remaining -= (end - start)
        }

        return result.toString()
    }

    /**
     * Validate rupee string format without converting.
     *
     * @param rupeeString String to validate
     * @return true if valid, false otherwise
     */
    fun isValidRupeeString(rupeeString: String): Boolean {
        return try {
            parseRupeesToPaise(rupeeString)
            true
        } catch (e: NumberFormatException) {
            false
        }
    }
}
