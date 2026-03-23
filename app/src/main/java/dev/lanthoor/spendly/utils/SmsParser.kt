package dev.lanthoor.spendly.utils

/**
 * Result of SMS parsing with confidence score.
 */
data class ParsedTransaction(
    val amount: Long,  // in paise (e.g., ₹100.50 = 10050)
    val transactionType: TransactionType,  // EXPENSE or INCOME
    val date: Long,  // timestamp in milliseconds
    val description: String,  // merchant name or transaction description
    val accountHint: String?,  // last 4 digits of account/card (e.g., "1234")
    val merchantName: String?,  // extracted merchant name if available
    val confidence: Float  // 0.0-1.0, higher = more confident
) {
    /**
     * Checks if this parsed transaction is reliable enough to show to user.
     * Threshold: 0.7 (70% confidence)
     */
    fun isReliable(): Boolean = confidence >= 0.7f
}

/**
 * Bank identifier with sender patterns.
 */
enum class BankIdentifier(val senderPatterns: List<Regex>) {
    HDFC(
        listOf(
            Regex("(?i)HDFCBK"),
            Regex("(?i)HDFC"),
            Regex("(?i)AD-HDFCBK")
        )
    ),
    ICICI(
        listOf(
            Regex("(?i)ICICIB"),
            Regex("(?i)ICICI"),
            Regex("(?i)iMobile")
        )
    ),
    SBI(
        listOf(
            Regex("(?i)SBISMS"),
            Regex("(?i)SBI"),
            Regex("(?i)SBIUPI")
        )
    ),
    AXIS(
        listOf(
            Regex("(?i)AXISBK"),
            Regex("(?i)AXIS")
        )
    ),
    KOTAK(
        listOf(
            Regex("(?i)KOTAKB"),
            Regex("(?i)KOTAK")
        )
    ),
    SCAPIA(
        listOf(
            Regex("(?i)(?:^|[-_])FEDSCP(?:[-_]|$)"),
            Regex("(?i)(?:^|[-_])SCAPIA(?:[-_]|$)")
        )
    ),
    UPI_NPCI(
        listOf(
            Regex("(?i)NPCI"),
            Regex("(?i)BHIMUPI")
        )
    ),
    UPI_PAYTM(
        listOf(
            Regex("(?i)PAYTM"),
            Regex("(?i)PYTMBA")
        )
    ),
    UPI_PHONEPE(
        listOf(
            Regex("(?i)PHONEPE"),
            Regex("(?i)PHNEPE")
        )
    ),
    UPI_GPAY(
        listOf(
            Regex("(?i)GPAY"),
            Regex("(?i)GooglePay")
        )
    ),
    UNKNOWN(listOf(Regex(".*")));  // Fallback

    companion object {
        fun identify(sender: String): BankIdentifier {
            return values().firstOrNull { bank ->
                bank.senderPatterns.any { it.containsMatchIn(sender) }
            } ?: UNKNOWN
        }
    }
}

/**
 * Service for parsing bank SMS messages into transaction data.
 *
 * This service uses regex patterns to extract transaction details from
 * SMS messages sent by Indian banks and UPI providers. Each bank has
 * unique SMS formats, so we maintain bank-specific parsing logic.
 *
 * **Confidence Scoring:**
 * - Amount found: +0.4
 * - Transaction type found: +0.3
 * - Account hint found: +0.2
 * - Merchant/description found: +0.1
 *
 * **Minimum confidence:** 0.7 (must find amount + type + either account or merchant)
 */
object SmsParser {
    private const val TAG = "SmsParser"

    /**
     * Attempts to parse a bank SMS message into transaction data.
     *
     * @param smsBody The SMS message body
     * @param sender The SMS sender address
     * @param smsTimestamp The timestamp when SMS was received (fallback for date)
     * @return ParsedTransaction if parsing successful and confidence >= 0.7, null otherwise
     */
    fun parseBankSms(smsBody: String, sender: String, smsTimestamp: Long): ParsedTransaction? {
        // Early rejection filters for non-transaction messages
        if (shouldRejectSms(smsBody)) {
            return null
        }

        val bank = BankIdentifier.identify(sender)

        val parsed = when (bank) {
            BankIdentifier.HDFC -> parseHdfcSms(smsBody, smsTimestamp)
            BankIdentifier.ICICI -> parseIciciSms(smsBody, smsTimestamp)
            BankIdentifier.SBI -> parseSbiSms(smsBody, smsTimestamp)
            BankIdentifier.AXIS -> parseAxisSms(smsBody, smsTimestamp)
            BankIdentifier.KOTAK -> parseKotakSms(smsBody, smsTimestamp)
            BankIdentifier.SCAPIA -> parseScapiaSms(smsBody, smsTimestamp)
            BankIdentifier.UPI_NPCI,
            BankIdentifier.UPI_PAYTM,
            BankIdentifier.UPI_PHONEPE,
            BankIdentifier.UPI_GPAY -> parseUpiSms(smsBody, smsTimestamp)

            BankIdentifier.UNKNOWN -> parseGenericBankSms(smsBody, smsTimestamp)
        }

        // Return only if confidence is high enough
        return parsed?.takeIf { it.isReliable() }
    }

    private fun shouldRejectSms(smsBody: String): Boolean {
        val lowerBody = smsBody.lowercase()

        if (lowerBody.contains("will be debited")) return true
        if (lowerBody.contains("will be credited")) return true
        if (lowerBody.contains("due on")) return true
        if (lowerBody.contains("emi of")) return true

        if (lowerBody.contains("balance is")) return true
        if (lowerBody.contains("avl bal:") && !lowerBody.contains("debited") && !lowerBody.contains(
                "credited"
            )
        ) {
            return true
        }

        if (lowerBody.contains("otp")) return true
        if (lowerBody.contains("verification code")) return true

        if (lowerBody.contains("bill of")) return true
        if (lowerBody.contains("statement")) return true

        return false
    }

    fun isKnownBankSender(sender: String): Boolean {
        return BankIdentifier.identify(sender) != BankIdentifier.UNKNOWN
    }

    private fun parseHdfcSms(smsBody: String, smsTimestamp: Long): ParsedTransaction? {
        val parser = dev.lanthoor.spendly.utils.parsers.HdfcParser()
        return parser.parse(smsBody, smsTimestamp)
    }

    // ============================================================
    // ICICI Bank SMS Parsing
    // ============================================================

    /**
     * Parses ICICI Bank SMS using IciciParser class.
     */
    private fun parseIciciSms(smsBody: String, smsTimestamp: Long): ParsedTransaction? {
        val parser = dev.lanthoor.spendly.utils.parsers.IciciParser()
        return parser.parse(smsBody, smsTimestamp)
    }

    private fun parseSbiSms(smsBody: String, smsTimestamp: Long): ParsedTransaction? {
        val parser = dev.lanthoor.spendly.utils.parsers.SbiParser()
        return parser.parse(smsBody, smsTimestamp)
    }

    private fun parseAxisSms(smsBody: String, smsTimestamp: Long): ParsedTransaction? {
        val parser = dev.lanthoor.spendly.utils.parsers.AxisParser()
        return parser.parse(smsBody, smsTimestamp)
    }

    private fun parseKotakSms(smsBody: String, smsTimestamp: Long): ParsedTransaction? {
        val parser = dev.lanthoor.spendly.utils.parsers.KotakParser()
        return parser.parse(smsBody, smsTimestamp)
    }

    private fun parseScapiaSms(smsBody: String, smsTimestamp: Long): ParsedTransaction? {
        val parser = dev.lanthoor.spendly.utils.parsers.ScapiaParser()
        return parser.parse(smsBody, smsTimestamp)
    }

    private fun parseUpiSms(smsBody: String, smsTimestamp: Long): ParsedTransaction? {
        val parser = dev.lanthoor.spendly.utils.parsers.UpiParser()
        return parser.parse(smsBody, smsTimestamp)
    }

    // ============================================================
    // Helper Methods
    // ============================================================

    /**
     * Converts amount string to paise (Long).
     * Handles formats: "1,234.56", "1234.5", "1234"
     */
    private fun parseAmountToPaise(amountStr: String): Long? {
        return try {
            // Remove commas and parse as double
            val cleaned = amountStr.replace(",", "")
            val rupees = cleaned.toDoubleOrNull() ?: return null
            // Convert to paise (multiply by 100)
            (rupees * 100).toLong()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Parses date from SMS format (13-Dec-24) to timestamp.
     */
    private fun parseDateFromSms(day: String, monthStr: String, year: String): Long {
        return try {
            val monthMap = mapOf(
                "jan" to 1, "feb" to 2, "mar" to 3, "apr" to 4,
                "may" to 5, "jun" to 6, "jul" to 7, "aug" to 8,
                "sep" to 9, "oct" to 10, "nov" to 11, "dec" to 12
            )

            val dayInt = day.toInt()
            val monthInt = monthMap[monthStr.lowercase()] ?: return System.currentTimeMillis()
            val yearInt = 2000 + year.toInt()  // "24" -> 2024

            java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.YEAR, yearInt)
                set(java.util.Calendar.MONTH, monthInt - 1)  // 0-indexed
                set(java.util.Calendar.DAY_OF_MONTH, dayInt)
                set(java.util.Calendar.HOUR_OF_DAY, 0)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }.timeInMillis
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    private fun parseGenericBankSms(smsBody: String, smsTimestamp: Long): ParsedTransaction? {
        val parser = dev.lanthoor.spendly.utils.parsers.GenericParser()
        return parser.parse(smsBody, smsTimestamp)
    }
}
