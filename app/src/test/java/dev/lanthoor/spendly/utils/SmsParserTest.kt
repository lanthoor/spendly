package dev.lanthoor.spendly.utils

import java.util.Calendar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

import dev.lanthoor.spendly.core.model.finance.TransactionType

/**
 * Comprehensive unit tests for SmsParser.
 *
 * Tests cover:
 * - All major Indian banks (HDFC, ICICI, SBI, Axis, Kotak)
 * - UPI providers (NPCI, PayTM, PhonePe, GPay)
 * - Amount parsing variations (commas, decimals, large numbers)
 * - Edge cases (failed transactions, balance inquiries, promotional SMS)
 * - Confidence scoring and threshold validation
 */
class SmsParserTest {

    private val timestamp = System.currentTimeMillis()

    // ============================================================
    // HDFC Bank Tests
    // ============================================================

    @Test
    fun `HDFC debit transaction parsed correctly`() {
        val sms = "INR 500.00 debited from A/c **1234 on 13-Dec-24. Avl Bal: INR 10,000.00"
        val parsed = SmsParser.parseBankSms(sms, "HDFCBK", timestamp)

        assertNotNull(parsed)
        assertEquals(50000L, parsed!!.amount)  // 500.00 rupees = 50000 paise
        assertEquals(TransactionType.EXPENSE, parsed.transactionType)
        assertEquals("1234", parsed.accountHint)
        assertTrue(parsed.confidence >= 0.7f)
        assertTrue(parsed.isReliable())
    }

    @Test
    fun `HDFC credit transaction parsed correctly`() {
        val sms = "Rs.200.00 credited to A/c **5678 on 12-Dec-24"
        val parsed = SmsParser.parseBankSms(sms, "HDFCBK", timestamp)

        assertNotNull(parsed)
        assertEquals(20000L, parsed!!.amount)
        assertEquals(TransactionType.INCOME, parsed.transactionType)
        assertEquals("5678", parsed.accountHint)
        assertTrue(parsed.isReliable())
    }

    @Test
    fun `HDFC card transaction with merchant parsed correctly`() {
        val sms = "INR 1,234.56 spent on HDFC Bank Card **9012 at AMAZON on 14-Dec-24"
        val parsed = SmsParser.parseBankSms(sms, "HDFCBK", timestamp)

        assertNotNull(parsed)
        assertEquals(123456L, parsed!!.amount)  // 1234.56 rupees = 123456 paise
        assertEquals(TransactionType.EXPENSE, parsed.transactionType)
        assertEquals("9012", parsed.accountHint)
        assertNotNull(parsed.merchantName)
        assertTrue(parsed.merchantName!!.contains("AMAZON", ignoreCase = true))
    }

    @Test
    fun `HDFC large amount with commas parsed correctly`() {
        val sms = "INR 1,23,456.78 debited from A/c **7890 on 16-Dec-24"
        val parsed = SmsParser.parseBankSms(sms, "HDFCBK", timestamp)

        assertNotNull(parsed)
        assertEquals(12345678L, parsed!!.amount)  // 123456.78 rupees = 12345678 paise
        assertEquals(TransactionType.EXPENSE, parsed.transactionType)
    }

    // ============================================================
    // ICICI Bank Tests
    // ============================================================

    @Test
    fun `ICICI debit transaction parsed correctly`() {
        val sms = "Rs.200.00 debited from A/C XX9876 on 13-Dec-24 for UPI/merchant@paytm"
        val parsed = SmsParser.parseBankSms(sms, "ICICIB", timestamp)

        assertNotNull(parsed)
        assertEquals(20000L, parsed!!.amount)
        assertEquals(TransactionType.EXPENSE, parsed.transactionType)
        assertEquals("9876", parsed.accountHint)
        assertNotNull(parsed.merchantName)
        assertTrue(parsed.isReliable())
    }

    @Test
    fun `ICICI credit transaction parsed correctly`() {
        val sms = "INR 500 credited to A/C XX1234 on 12-Dec-24"
        val parsed = SmsParser.parseBankSms(sms, "ICICIB", timestamp)

        assertNotNull(parsed)
        assertEquals(50000L, parsed!!.amount)
        assertEquals(TransactionType.INCOME, parsed.transactionType)
        assertEquals("1234", parsed.accountHint)
    }

    @Test
    fun `ICICI large amount parsed correctly`() {
        val sms = "Rs.25,000 debited from A/C XX2222 on 18-Dec-24 for rent payment"
        val parsed = SmsParser.parseBankSms(sms, "ICICIB", timestamp)

        assertNotNull(parsed)
        assertEquals(2500000L, parsed!!.amount)  // 25000 rupees = 2500000 paise
        assertEquals(TransactionType.EXPENSE, parsed.transactionType)
    }

    // ============================================================
    // SBI Tests
    // ============================================================

    @Test
    fun `SBI debit transaction parsed correctly`() {
        val sms = "Rs 1000 debited from Acct XX1234 on 13Dec24"
        val parsed = SmsParser.parseBankSms(sms, "SBISMS", timestamp)

        assertNotNull(parsed)
        assertEquals(100000L, parsed!!.amount)
        assertEquals(TransactionType.EXPENSE, parsed.transactionType)
        assertEquals("1234", parsed.accountHint)
        assertTrue(parsed.isReliable())
    }

    @Test
    fun `SBI credit transaction parsed correctly`() {
        val sms = "Rs.500.00 credited to Acct XX5678 on 12Dec24"
        val parsed = SmsParser.parseBankSms(sms, "SBISMS", timestamp)

        assertNotNull(parsed)
        assertEquals(50000L, parsed!!.amount)
        assertEquals(TransactionType.INCOME, parsed.transactionType)
        assertEquals("5678", parsed.accountHint)
    }

    @Test
    fun `SBI without decimal parsed correctly`() {
        val sms = "Rs 50 debited from Acct XX9012 on 15Dec24"
        val parsed = SmsParser.parseBankSms(sms, "SBI", timestamp)

        assertNotNull(parsed)
        assertEquals(5000L, parsed!!.amount)  // 50 rupees = 5000 paise
        assertEquals(TransactionType.EXPENSE, parsed.transactionType)
    }

    // ============================================================
    // Axis Bank Tests
    // ============================================================

    @Test
    fun `Axis debit transaction parsed correctly`() {
        val sms = "INR 750.00 debited from A/c **2345 on 14-Dec-24"
        val parsed = SmsParser.parseBankSms(sms, "AXISBK", timestamp)

        assertNotNull(parsed)
        assertEquals(75000L, parsed!!.amount)
        assertEquals(TransactionType.EXPENSE, parsed.transactionType)
        assertEquals("2345", parsed.accountHint)
        assertTrue(parsed.isReliable())
    }

    @Test
    fun `Axis credit transaction parsed correctly`() {
        val sms = "Rs.1000 credited to A/c **6789 on 15-Dec-24"
        val parsed = SmsParser.parseBankSms(sms, "AXIS", timestamp)

        assertNotNull(parsed)
        assertEquals(100000L, parsed!!.amount)
        assertEquals(TransactionType.INCOME, parsed.transactionType)
    }

    @Test
    fun `Axis card with merchant parsed correctly`() {
        val sms = "INR 450.00 spent on Axis Bank Card **1234 at FLIPKART on 17-Dec-24"
        val parsed = SmsParser.parseBankSms(sms, "AXISBK", timestamp)

        assertNotNull(parsed)
        assertEquals(45000L, parsed!!.amount)
        assertEquals(TransactionType.EXPENSE, parsed.transactionType)
        assertNotNull(parsed.merchantName)
    }

    // ============================================================
    // Kotak Bank Tests
    // ============================================================

    @Test
    fun `Kotak debit transaction parsed correctly`() {
        val sms = "Rs.300.00 debited from A/c **3456 on 16-Dec-24"
        val parsed = SmsParser.parseBankSms(sms, "KOTAKB", timestamp)

        assertNotNull(parsed)
        assertEquals(30000L, parsed!!.amount)
        assertEquals(TransactionType.EXPENSE, parsed.transactionType)
        assertEquals("3456", parsed.accountHint)
    }

    @Test
    fun `Kotak credit transaction parsed correctly`() {
        val sms = "INR 2000 credited to A/c **7890 on 17-Dec-24"
        val parsed = SmsParser.parseBankSms(sms, "KOTAK", timestamp)

        assertNotNull(parsed)
        assertEquals(200000L, parsed!!.amount)
        assertEquals(TransactionType.INCOME, parsed.transactionType)
    }

    // ============================================================
    // Scapia Tests
    // ============================================================

    @Test
    fun `Scapia successful txn parsed as expense`() {
        val sms = "Your txn of Rs.1,234.50 at Obsidian + Ca on your Scapia Federal Visa Card was successful on 13-Dec-24"
        val parsed = SmsParser.parseBankSms(sms, "TX-FEDSCP-S", timestamp)

        assertNotNull(parsed)
        assertEquals(123450L, parsed!!.amount)
        assertEquals(TransactionType.EXPENSE, parsed.transactionType)
        assertTrue(parsed.isReliable())
    }

    @Test
    fun `Scapia merchant extraction supports mixed case and symbols`() {
        val sms = "A transaction of INR 845.00 at Cloudflare + Us. on your Scapia Federal RuPay Card was successful on 14-Dec-24"
        val parsed = SmsParser.parseBankSms(sms, "VM-FEDSCP-S", timestamp)

        assertNotNull(parsed)
        assertEquals(TransactionType.EXPENSE, parsed!!.transactionType)
        assertEquals("Cloudflare + Us", parsed.merchantName)
    }

    @Test
    fun `Scapia spent phrase is parsed as expense`() {
        val sms = "INR 599.00 spent on your Scapia Federal Visa Card at Obsidian + Ca on 15-Dec-24"
        val parsed = SmsParser.parseBankSms(sms, "TX-FEDSCP-S", timestamp)

        assertNotNull(parsed)
        assertEquals(59900L, parsed!!.amount)
        assertEquals(TransactionType.EXPENSE, parsed.transactionType)
    }

    @Test
    fun `Scapia success-only message should not infer expense`() {
        val sms = "Your Scapia Federal Visa Card verification was successful on 15-Dec-24"
        val parsed = SmsParser.parseBankSms(sms, "TX-FEDSCP-S", timestamp)

        assertNull(parsed)
    }

    @Test
    fun `Scapia sender variants are recognized as known senders`() {
        assertTrue(SmsParser.isKnownBankSender("FEDSCP"))
        assertTrue(SmsParser.isKnownBankSender("TX-FEDSCP-S"))
        assertTrue(SmsParser.isKnownBankSender("VM-FEDSCP-S"))
        assertTrue(SmsParser.isKnownBankSender("AD-SCAPIA-T"))
    }

    // ============================================================
    // UPI Tests
    // ============================================================

    @Test
    fun `UPI PayTM debit transaction parsed correctly`() {
        val sms = "Rs.150 debited from your account via UPI to merchant@paytm on 13-Dec-24"
        val parsed = SmsParser.parseBankSms(sms, "PAYTM", timestamp)

        assertNotNull(parsed)
        assertEquals(15000L, parsed!!.amount)
        assertEquals(TransactionType.EXPENSE, parsed.transactionType)
        assertNotNull(parsed.merchantName)
        assertTrue(parsed.merchantName!!.contains("@paytm", ignoreCase = true))
    }

    @Test
    fun `UPI PayTM credit transaction parsed correctly`() {
        val sms = "Rs.500 credited to your account via UPI from sender@paytm on 14-Dec-24"
        val parsed = SmsParser.parseBankSms(sms, "PYTMBA", timestamp)

        assertNotNull(parsed)
        assertEquals(50000L, parsed!!.amount)
        assertEquals(TransactionType.INCOME, parsed.transactionType)
    }

    @Test
    fun `UPI PhonePe sent transaction parsed correctly`() {
        val sms = "Rs.200 sent to merchant@phonepe via UPI on 15-Dec-24"
        val parsed = SmsParser.parseBankSms(sms, "PHONEPE", timestamp)

        assertNotNull(parsed)
        assertEquals(20000L, parsed!!.amount)
        assertEquals(TransactionType.EXPENSE, parsed.transactionType)
    }

    @Test
    fun `UPI PhonePe received transaction parsed correctly`() {
        val sms = "Rs.1,000 received from user@phonepe via UPI on 16-Dec-24"
        val parsed = SmsParser.parseBankSms(sms, "PHNEPE", timestamp)

        assertNotNull(parsed)
        assertEquals(100000L, parsed!!.amount)
        assertEquals(TransactionType.INCOME, parsed.transactionType)
    }

    @Test
    fun `UPI GPay transaction parsed correctly`() {
        val sms = "Rs.75.50 debited from your account to merchant@googlepay on 17-Dec-24"
        val parsed = SmsParser.parseBankSms(sms, "GPAY", timestamp)

        assertNotNull(parsed)
        assertEquals(7550L, parsed!!.amount)
        assertEquals(TransactionType.EXPENSE, parsed.transactionType)
    }

    @Test
    fun `UPI NPCI transaction parsed correctly`() {
        val sms = "UPI-NPCI: Rs.200 credited to A/c XX1234 from sender@upi on 19-Dec-24"
        val parsed = SmsParser.parseBankSms(sms, "NPCI", timestamp)

        assertNotNull(parsed)
        assertEquals(20000L, parsed!!.amount)
        assertEquals(TransactionType.INCOME, parsed.transactionType)
    }

    @Test
    fun `UPI phone number format parsed correctly`() {
        val sms = "Rs.100 sent to 9876543210@paytm via UPI on 21-Dec-24"
        val parsed = SmsParser.parseBankSms(sms, "PAYTM", timestamp)

        assertNotNull(parsed)
        assertEquals(10000L, parsed!!.amount)
        assertEquals(TransactionType.EXPENSE, parsed.transactionType)
    }

    // ============================================================
    // Edge Cases & Negative Tests
    // ============================================================

    @Test
    fun `Failed transaction should not be parsed`() {
        val sms = "Transaction of Rs.500 FAILED due to insufficient balance"
        val parsed = SmsParser.parseBankSms(sms, "HDFCBK", timestamp)

        assertNull(parsed)  // Should not parse failed transactions
    }

    @Test
    fun `Balance inquiry should not be parsed`() {
        val sms = "Your A/c XX1234 balance is Rs.10,000.00"
        val parsed = SmsParser.parseBankSms(sms, "HDFCBK", timestamp)

        assertNull(parsed)  // Should not parse balance inquiries
    }

    @Test
    fun `Promotional SMS should not be parsed`() {
        val sms = "Get 10% cashback on your next purchase with HDFC credit card!"
        val parsed = SmsParser.parseBankSms(sms, "HDFCBK", timestamp)

        assertNull(parsed)  // Should not parse promotional messages
    }

    @Test
    fun `OTP message should not be parsed`() {
        val sms = "Your OTP for HDFC Bank is 123456"
        val parsed = SmsParser.parseBankSms(sms, "HDFCBK", timestamp)

        assertNull(parsed)
    }

    @Test
    fun `EMI notification should not be parsed`() {
        val sms = "EMI of Rs.5000 will be debited on 1st of every month"
        val parsed = SmsParser.parseBankSms(sms, "HDFCBK", timestamp)

        assertNull(parsed)
    }

    @Test
    fun `Credit card bill should not be parsed`() {
        val sms = "Your HDFC Credit Card bill of Rs.15,000 is due on 25-Dec-24"
        val parsed = SmsParser.parseBankSms(sms, "HDFCBK", timestamp)

        assertNull(parsed)
    }

    @Test
    fun `Malformed amount should not be parsed`() {
        val sms = "INR ABC debited from A/c **1234"
        val parsed = SmsParser.parseBankSms(sms, "HDFCBK", timestamp)

        assertNull(parsed)  // Should not parse with invalid amount
    }

    @Test
    fun `Unknown sender should be parsed with generic parser`() {
        val sms = "INR 500 debited from A/c **1234"
        val parsed = SmsParser.parseBankSms(sms, "SPAM-123", timestamp)

        assertNotNull(parsed)  // Now parses unknown senders with amount + type
        assertEquals(50000L, parsed!!.amount)  // 500 rupees = 50000 paise
        assertEquals(TransactionType.EXPENSE, parsed.transactionType)
        assertEquals("Bank Transaction (SMS)", parsed.description)
        assertEquals(0.9f, parsed.confidence, 0.01f)  // 0.4 (amount) + 0.3 (type) + 0.2 (account)
        assertEquals("1234", parsed.accountHint)  // Generic parser now extracts account hints
        assertNull(parsed.merchantName)  // No merchant in this SMS
    }

    @Test
    fun `Low confidence parsing should be rejected`() {
        // SMS with only amount, no transaction type or account
        val sms = "Rs.500"
        val parsed = SmsParser.parseBankSms(sms, "HDFCBK", timestamp)

        assertNull(parsed)  // Confidence < 0.7, should be rejected
    }

    @Test
    fun `Declined transaction should not be parsed`() {
        val sms = "UPI payment of Rs.200 DECLINED by bank"
        val parsed = SmsParser.parseBankSms(sms, "PAYTM", timestamp)

        assertNull(parsed)
    }

    // ============================================================
    // Amount Parsing Tests
    // ============================================================

    @Test
    fun `Amount with Indian comma format parsed correctly`() {
        val sms = "INR 1,23,456.78 debited from A/c **1234 on 13-Dec-24"
        val parsed = SmsParser.parseBankSms(sms, "HDFCBK", timestamp)

        assertNotNull(parsed)
        assertEquals(12345678L, parsed!!.amount)  // 123456.78 rupees = 12345678 paise
    }

    @Test
    fun `Amount without decimal parsed correctly`() {
        val sms = "Rs 500 debited from Acct XX1234 on 13Dec24"
        val parsed = SmsParser.parseBankSms(sms, "SBISMS", timestamp)

        assertNotNull(parsed)
        assertEquals(50000L, parsed!!.amount)  // 500 rupees = 50000 paise
    }

    @Test
    fun `Amount with single decimal place parsed correctly`() {
        val sms = "Rs.100.5 debited from A/c **3456 on 16-Dec-24"
        val parsed = SmsParser.parseBankSms(sms, "HDFCBK", timestamp)

        assertNotNull(parsed)
        assertEquals(10050L, parsed!!.amount)  // 100.5 rupees = 10050 paise
    }

    @Test
    fun `Large amount parsed correctly`() {
        val sms = "Rs.99,999.99 debited from A/c **7890 on 17-Dec-24"
        val parsed = SmsParser.parseBankSms(sms, "HDFCBK", timestamp)

        assertNotNull(parsed)
        assertEquals(9999999L, parsed!!.amount)  // 99999.99 rupees = 9999999 paise
    }

    @Test
    fun `Small amount parsed correctly`() {
        val sms = "Rs.1.00 debited from A/c **2222 on 19-Dec-24"
        val parsed = SmsParser.parseBankSms(sms, "HDFCBK", timestamp)

        assertNotNull(parsed)
        assertEquals(100L, parsed!!.amount)  // 1.00 rupee = 100 paise
    }

    @Test
    fun `Amount with Rs prefix and no space parsed correctly`() {
        val sms = "Rs.2,500.00 debited from Acct XX5678 on 14Dec24"
        val parsed = SmsParser.parseBankSms(sms, "SBISMS", timestamp)

        assertNotNull(parsed)
        assertEquals(250000L, parsed!!.amount)
    }

    // ============================================================
    // Timestamp Preservation Tests
    // ============================================================

    @Test
    fun `HDFC parsed date keeps sms timestamp time`() {
        val smsTimestamp = buildTimestamp(2025, Calendar.JANUARY, 20, 18, 47, 33, 789)
        val sms = "INR 500.00 debited from A/c **1234 on 13-Dec-24"

        val parsed = SmsParser.parseBankSms(sms, "HDFCBK", smsTimestamp)

        assertNotNull(parsed)
        val parsedCalendar = Calendar.getInstance().apply { timeInMillis = parsed!!.date }
        assertEquals(2024, parsedCalendar.get(Calendar.YEAR))
        assertEquals(Calendar.DECEMBER, parsedCalendar.get(Calendar.MONTH))
        assertEquals(13, parsedCalendar.get(Calendar.DAY_OF_MONTH))
        assertEquals(18, parsedCalendar.get(Calendar.HOUR_OF_DAY))
        assertEquals(47, parsedCalendar.get(Calendar.MINUTE))
        assertEquals(33, parsedCalendar.get(Calendar.SECOND))
        assertEquals(789, parsedCalendar.get(Calendar.MILLISECOND))
    }

    @Test
    fun `Generic parser date keeps sms timestamp time`() {
        val smsTimestamp = buildTimestamp(2026, Calendar.FEBRUARY, 5, 9, 12, 45, 120)
        val sms = "INR 899 debited from A/c **7788 on 13/12/24"

        val parsed = SmsParser.parseBankSms(sms, "SPAM-123", smsTimestamp)

        assertNotNull(parsed)
        val parsedCalendar = Calendar.getInstance().apply { timeInMillis = parsed!!.date }
        assertEquals(2024, parsedCalendar.get(Calendar.YEAR))
        assertEquals(Calendar.DECEMBER, parsedCalendar.get(Calendar.MONTH))
        assertEquals(13, parsedCalendar.get(Calendar.DAY_OF_MONTH))
        assertEquals(9, parsedCalendar.get(Calendar.HOUR_OF_DAY))
        assertEquals(12, parsedCalendar.get(Calendar.MINUTE))
        assertEquals(45, parsedCalendar.get(Calendar.SECOND))
        assertEquals(120, parsedCalendar.get(Calendar.MILLISECOND))
    }

    @Test
    fun `Two digit year uses sms timestamp century`() {
        val smsTimestamp = buildTimestamp(1999, Calendar.JANUARY, 2, 11, 10, 9, 8)
        val sms = "INR 899 debited from A/c **7788 on 13/12/99"

        val parsed = SmsParser.parseBankSms(sms, "SPAM-123", smsTimestamp)

        assertNotNull(parsed)
        val parsedCalendar = Calendar.getInstance().apply { timeInMillis = parsed!!.date }
        assertEquals(1999, parsedCalendar.get(Calendar.YEAR))
        assertEquals(Calendar.DECEMBER, parsedCalendar.get(Calendar.MONTH))
        assertEquals(13, parsedCalendar.get(Calendar.DAY_OF_MONTH))
        assertEquals(11, parsedCalendar.get(Calendar.HOUR_OF_DAY))
        assertEquals(10, parsedCalendar.get(Calendar.MINUTE))
        assertEquals(9, parsedCalendar.get(Calendar.SECOND))
        assertEquals(8, parsedCalendar.get(Calendar.MILLISECOND))
    }

    // ============================================================
    // Confidence Scoring Tests
    // ============================================================

    @Test
    fun `High confidence transaction with all fields`() {
        val sms = "INR 500.00 debited from A/c **1234 at AMAZON on 13-Dec-24"
        val parsed = SmsParser.parseBankSms(sms, "HDFCBK", timestamp)

        assertNotNull(parsed)
        assertTrue(parsed!!.confidence >= 0.9f)  // Amount + Type + Account + Merchant
    }

    @Test
    fun `Medium confidence transaction without merchant`() {
        val sms = "INR 500.00 debited from A/c **1234 on 13-Dec-24"
        val parsed = SmsParser.parseBankSms(sms, "HDFCBK", timestamp)

        assertNotNull(parsed)
        assertTrue(parsed!!.confidence >= 0.7f)  // Amount + Type + Account
        assertTrue(parsed.confidence < 1.0f)
    }

    @Test
    fun `isReliable returns true for confidence above threshold`() {
        val sms = "Rs.200 debited from A/C XX9876 on 13-Dec-24"
        val parsed = SmsParser.parseBankSms(sms, "ICICIB", timestamp)

        assertNotNull(parsed)
        assertTrue(parsed!!.isReliable())
        assertTrue(parsed.confidence >= 0.7f)
    }

    // ============================================================
    // Bank Identifier Tests
    // ============================================================

    @Test
    fun `isKnownBankSender recognizes HDFC variants`() {
        assertTrue(SmsParser.isKnownBankSender("HDFCBK"))
        assertTrue(SmsParser.isKnownBankSender("HDFC"))
        assertTrue(SmsParser.isKnownBankSender("AD-HDFCBK"))
    }

    @Test
    fun `isKnownBankSender recognizes ICICI variants`() {
        assertTrue(SmsParser.isKnownBankSender("ICICIB"))
        assertTrue(SmsParser.isKnownBankSender("ICICI"))
        assertTrue(SmsParser.isKnownBankSender("iMobile"))
    }

    @Test
    fun `isKnownBankSender recognizes SBI variants`() {
        assertTrue(SmsParser.isKnownBankSender("SBISMS"))
        assertTrue(SmsParser.isKnownBankSender("SBI"))
        assertTrue(SmsParser.isKnownBankSender("SBIUPI"))
    }

    @Test
    fun `isKnownBankSender recognizes UPI providers`() {
        assertTrue(SmsParser.isKnownBankSender("PAYTM"))
        assertTrue(SmsParser.isKnownBankSender("PHONEPE"))
        assertTrue(SmsParser.isKnownBankSender("GPAY"))
        assertTrue(SmsParser.isKnownBankSender("NPCI"))
    }

    @Test
    fun `isKnownBankSender rejects unknown senders`() {
        assertFalse(SmsParser.isKnownBankSender("SPAM123"))
        assertFalse(SmsParser.isKnownBankSender("UNKNOWN"))
        assertFalse(SmsParser.isKnownBankSender(""))
    }

    // ============================================================
    // Batch Tests with SmsSamples
    // ============================================================

    @Test
    fun `All HDFC samples parse successfully`() {
        var successCount = 0
        SmsSamples.HDFC_SAMPLES.forEach { sms ->
            val parsed = SmsParser.parseBankSms(sms, "HDFCBK", timestamp)
            if (parsed != null && parsed.isReliable()) {
                successCount++
            }
        }
        assertTrue(
            "Expected at least 80% success rate for HDFC",
            successCount >= (SmsSamples.HDFC_SAMPLES.size * 0.8).toInt()
        )
    }

    @Test
    fun `All ICICI samples parse successfully`() {
        var successCount = 0
        SmsSamples.ICICI_SAMPLES.forEach { sms ->
            val parsed = SmsParser.parseBankSms(sms, "ICICIB", timestamp)
            if (parsed != null && parsed.isReliable()) {
                successCount++
            }
        }
        assertTrue(
            "Expected at least 80% success rate for ICICI",
            successCount >= (SmsSamples.ICICI_SAMPLES.size * 0.8).toInt()
        )
    }

    @Test
    fun `All SBI samples parse successfully`() {
        var successCount = 0
        SmsSamples.SBI_SAMPLES.forEach { sms ->
            val parsed = SmsParser.parseBankSms(sms, "SBISMS", timestamp)
            if (parsed != null && parsed.isReliable()) {
                successCount++
            }
        }
        assertTrue(
            "Expected at least 80% success rate for SBI",
            successCount >= (SmsSamples.SBI_SAMPLES.size * 0.8).toInt()
        )
    }

    @Test
    fun `All UPI samples parse successfully`() {
        var successCount = 0
        val senders = listOf("PAYTM", "PHONEPE", "GPAY", "NPCI")
        SmsSamples.UPI_SAMPLES.forEach { sms ->
            for (sender in senders) {
                val parsed = SmsParser.parseBankSms(sms, sender, timestamp)
                if (parsed != null && parsed.isReliable()) {
                    successCount++
                    break
                }
            }
        }
        assertTrue(
            "Expected at least 70% success rate for UPI",
            successCount >= (SmsSamples.UPI_SAMPLES.size * 0.7).toInt()
        )
    }

    @Test
    fun `All negative samples should NOT parse`() {
        SmsSamples.NEGATIVE_SAMPLES.forEach { sms ->
            val parsed = SmsParser.parseBankSms(sms, "HDFCBK", timestamp)
            assertNull("Negative sample should not parse: $sms", parsed)
        }
    }

    // ============================================================
    // New Keywords Tests (withdrawn, deposited)
    // ============================================================

    @Test
    fun `Withdrawn keyword should be parsed as EXPENSE`() {
        val sms = "Rs.500 withdrawn from A/c **1234 on 13-Dec-24"
        val parsed = SmsParser.parseBankSms(sms, "HDFCBK", timestamp)

        assertNotNull(parsed)
        assertEquals(50000L, parsed!!.amount)
        assertEquals(TransactionType.EXPENSE, parsed.transactionType)
        assertEquals("1234", parsed.accountHint)
        assertTrue(parsed.isReliable())
    }

    @Test
    fun `Deposited keyword should be parsed as INCOME`() {
        val sms = "Rs.1000 deposited to A/c **5678 on 14-Dec-24"
        val parsed = SmsParser.parseBankSms(sms, "HDFCBK", timestamp)

        assertNotNull(parsed)
        assertEquals(100000L, parsed!!.amount)
        assertEquals(TransactionType.INCOME, parsed.transactionType)
        assertEquals("5678", parsed.accountHint)
        assertTrue(parsed.isReliable())
    }

    @Test
    fun `ATM withdrawal with withdrawn keyword should parse correctly`() {
        val sms = "INR 2000.00 withdrawn from Card **9999 at ATM on 15-Dec-24"
        val parsed = SmsParser.parseBankSms(sms, "AXISBK", timestamp)

        assertNotNull(parsed)
        assertEquals(200000L, parsed!!.amount)
        assertEquals(TransactionType.EXPENSE, parsed.transactionType)
        assertEquals("9999", parsed.accountHint)
        assertTrue(parsed.isReliable())
    }

    @Test
    fun `Cash deposited transaction should parse correctly`() {
        val sms = "Rs 5000.00 deposited to Acct XX3456 on 16Dec24"
        val parsed = SmsParser.parseBankSms(sms, "SBISMS", timestamp)

        assertNotNull(parsed)
        assertEquals(500000L, parsed!!.amount)
        assertEquals(TransactionType.INCOME, parsed.transactionType)
        assertEquals("3456", parsed.accountHint)
        assertTrue(parsed.isReliable())
    }

    private fun buildTimestamp(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
        second: Int,
        millisecond: Int
    ): Long {
        return Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, second)
            set(Calendar.MILLISECOND, millisecond)
        }.timeInMillis
    }
}
