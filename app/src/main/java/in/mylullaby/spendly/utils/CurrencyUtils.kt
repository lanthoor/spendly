package `in`.mylullaby.spendly.utils

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
     * Format paise as rupee string (ZERO precision loss).
     *
     * Uses integer division/modulo - no floating-point operations.
     *
     * Examples:
     * - 12345 paise → "₹123.45"
     * - 100 paise → "₹1.00"
     * - 99 paise → "₹0.99"
     *
     * @param paise Amount in paise
     * @return Formatted string (e.g., "₹123.45")
     */
    fun formatPaise(paise: Long): String {
        val rupees = paise / PAISE_PER_RUPEE
        val paiseRemainder = paise % PAISE_PER_RUPEE
        return "₹%d.%02d".format(rupees, paiseRemainder)
    }

    /**
     * Convert paise to rupee string for form input (ZERO precision loss).
     *
     * Uses integer division/modulo - no floating-point operations.
     * Returns plain decimal format (never scientific notation).
     *
     * Examples:
     * - 12345 paise → "123.45"
     * - 100 paise → "1.00"
     * - 99 paise → "0.99"
     * - 10000000 paise (₹100,000) → "100000.00" (not "1E5")
     *
     * @param paise Amount in paise
     * @return Plain decimal string (e.g., "123.45")
     */
    fun paiseToRupeeString(paise: Long): String {
        val rupees = paise / PAISE_PER_RUPEE
        val paiseRemainder = paise % PAISE_PER_RUPEE
        return "%d.%02d".format(rupees, paiseRemainder)
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
