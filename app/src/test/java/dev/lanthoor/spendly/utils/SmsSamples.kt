package dev.lanthoor.spendly.utils

/**
 * Real-world SMS samples for testing SMS parser.
 *
 * These samples represent actual SMS formats from Indian banks and UPI providers.
 * Names, account numbers, and amounts have been anonymized.
 */
object SmsSamples {

    // ============================================================
    // HDFC Bank Samples
    // ============================================================

    val HDFC_SAMPLES = listOf(
        // Debit transactions
        "INR 500.00 debited from A/c **1234 on 13-Dec-24. Avl Bal: INR 10,000.00",
        "Rs.1,234.56 debited from A/c **5678 on 14-Dec-24. Avl Bal: Rs.50,000.00",
        "INR 250.00 spent on HDFC Bank Card **9012 at AMAZON on 15-Dec-24",
        "Rs.75.50 debited from A/c **3456 on 12-Dec-24 for UPI txn",

        // Credit transactions
        "Rs.200.00 credited to A/c **5678 on 12-Dec-24",
        "INR 5,000 credited to A/c **1234 on 13-Dec-24",

        // Large amounts with commas
        "INR 1,23,456.78 debited from A/c **7890 on 16-Dec-24",

        // Different merchant formats
        "INR 999.00 spent on HDFC Bank Card **1111 at SWIGGY DELIVERY on 17-Dec-24",
        "Rs.150 spent on HDFC Bank Card **2222 at ZOMATO on 18-Dec-24"
    )

    // ============================================================
    // ICICI Bank Samples
    // ============================================================

    val ICICI_SAMPLES = listOf(
        // Debit transactions
        "Rs.200.00 debited from A/C XX9876 on 13-Dec-24 for UPI/merchant@paytm",
        "INR 1,500 debited from A/C XX1234 on 14-Dec-24 for merchant payment",
        "Rs.50.00 debited from A/C XX5555 on 15-Dec-24 for UPI/shop@phonepe",

        // Credit transactions
        "INR 500 credited to A/C XX1234 on 12-Dec-24",
        "Rs.10,000.00 credited to A/C XX6789 on 13-Dec-24 for salary",

        // UPI format
        "Rs.300 debited from A/C XX4444 on 16-Dec-24 for UPI/9876543210@paytm",

        // Without description
        "INR 750.50 debited from A/C XX3333 on 17-Dec-24",

        // Large amount
        "Rs.25,000 debited from A/C XX2222 on 18-Dec-24 for rent payment"
    )

    // ============================================================
    // SBI Samples
    // ============================================================

    val SBI_SAMPLES = listOf(
        // Debit transactions
        "Rs 1000 debited from Acct XX1234 on 13Dec24",
        "Rs.2,500.00 debited from Acct XX5678 on 14Dec24",
        "Rs 50 debited from Acct XX9012 on 15Dec24",

        // Credit transactions
        "Rs.500.00 credited to Acct XX5678 on 12Dec24",
        "Rs 15,000 credited to Acct XX1234 on 13Dec24",

        // No decimal
        "Rs 100 debited from Acct XX3456 on 16Dec24",
        "Rs 5000 credited to Acct XX7890 on 17Dec24",

        // With decimal
        "Rs.123.45 debited from Acct XX1111 on 18Dec24"
    )

    // ============================================================
    // Axis Bank Samples
    // ============================================================

    val AXIS_SAMPLES = listOf(
        // Debit transactions
        "INR 750.00 debited from A/c **2345 on 14-Dec-24",
        "Rs.1,200 debited from A/c **6789 on 15-Dec-24",

        // Credit transactions
        "Rs.1000 credited to A/c **6789 on 15-Dec-24",
        "INR 3,500.00 credited to A/c **2345 on 16-Dec-24",

        // With merchant
        "INR 450.00 spent on Axis Bank Card **1234 at FLIPKART on 17-Dec-24",

        // Large amount
        "Rs.50,000 debited from A/c **9999 on 18-Dec-24"
    )

    // ============================================================
    // Kotak Bank Samples
    // ============================================================

    val KOTAK_SAMPLES = listOf(
        // Debit transactions
        "Rs.300.00 debited from A/c **3456 on 16-Dec-24",
        "INR 1,800 debited from A/c **7890 on 17-Dec-24",

        // Credit transactions
        "INR 2000 credited to A/c **7890 on 17-Dec-24",
        "Rs.8,500.00 credited to A/c **3456 on 18-Dec-24",

        // Various amounts
        "Rs.99.99 debited from A/c **1111 on 19-Dec-24",
        "INR 12,345.67 credited to A/c **2222 on 20-Dec-24"
    )

    // ============================================================
    // UPI Samples
    // ============================================================

    val UPI_SAMPLES = listOf(
        // PayTM
        "Rs.150 debited from your account via UPI to merchant@paytm on 13-Dec-24",
        "Rs.500 credited to your account via UPI from sender@paytm on 14-Dec-24",

        // PhonePe
        "Rs.200 sent to merchant@phonepe via UPI on 15-Dec-24",
        "Rs.1,000 received from user@phonepe via UPI on 16-Dec-24",

        // GPay
        "Rs.75.50 debited from your account to merchant@googlepay on 17-Dec-24",
        "Rs.250 credited to your account from sender@gpay on 18-Dec-24",

        // NPCI/BHIM
        "UPI-NPCI: Rs.200 credited to A/c XX1234 from sender@upi on 19-Dec-24",
        "Rs.300 debited for UPI txn to merchant@bhimupi on 20-Dec-24",

        // Phone number format
        "Rs.100 sent to 9876543210@paytm via UPI on 21-Dec-24"
    )

    // ============================================================
    // Edge Cases & Negative Samples (should NOT parse)
    // ============================================================

    val NEGATIVE_SAMPLES = listOf(
        // Failed transactions
        "Transaction of Rs.500 FAILED due to insufficient balance",
        "UPI payment of Rs.200 DECLINED by bank",
        "Rs.1000 transaction could not be completed",

        // Balance inquiry (not a transaction)
        "Your A/c XX1234 balance is Rs.10,000.00",
        "Available balance: Rs.50,000.00 in A/c **5678",
        "A/c XX9876: Avl Bal Rs.25,000",

        // Promotional messages
        "Get 10% cashback on your next purchase with HDFC credit card!",
        "Special offer: Flat Rs.500 off on shopping",
        "Congratulations! You won Rs.1000 cashback",

        // OTP messages
        "Your OTP for HDFC Bank is 123456",
        "OTP: 654321 for ICICI iMobile",

        // EMI notifications
        "EMI of Rs.5000 will be debited on 1st of every month",
        "Your EMI payment of Rs.3000 is due on 15-Jan-25",

        // Credit card bills
        "Your HDFC Credit Card bill of Rs.15,000 is due on 25-Dec-24",
        "Credit Card Statement: Total due Rs.20,000",

        // Limit alerts
        "You have exceeded your credit limit of Rs.50,000",
        "Low balance alert: A/c **1234 balance below Rs.1000",

        // Malformed amounts
        "INR ABC debited from A/c **1234",
        "Rs. invalid amount credited",

        // Missing transaction type
        "Rs.500 from A/c **1234 on 13-Dec-24",

        // Partial information
        "Rs.200 on 14-Dec-24",
        "Debited from A/c **5678"
    )

    // ============================================================
    // Amount Parsing Test Cases
    // ============================================================

    val AMOUNT_VARIATIONS = listOf(
        // With commas
        "INR 1,234.56 debited from A/c **1234 on 13-Dec-24",
        "Rs.12,345.67 debited from A/c **5678 on 14-Dec-24",
        "INR 1,23,456.78 debited from A/c **9012 on 15-Dec-24",

        // Without decimal
        "Rs 500 debited from Acct XX1234 on 13Dec24",
        "INR 1000 credited to A/C XX5678 on 14-Dec-24",

        // With single decimal
        "Rs.100.5 debited from A/c **3456 on 16-Dec-24",

        // Large amounts
        "INR 99,999.99 debited from A/c **7890 on 17-Dec-24",
        "Rs.1,00,000.00 credited to Acct XX1111 on 18-Dec-24",

        // Small amounts
        "Rs.1.00 debited from A/c **2222 on 19-Dec-24",
        "INR 0.50 debited from A/c **3333 on 20-Dec-24"
    )
}
