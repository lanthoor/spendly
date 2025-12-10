package `in`.mylullaby.spendly.utils

/**
 * Payment methods supported by the app.
 * Used for expense transactions.
 */
enum class PaymentMethod {
    CASH,
    UPI,
    DEBIT_CARD,
    CREDIT_CARD,
    NET_BANKING,
    WALLET;

    companion object {
        fun fromString(value: String): PaymentMethod? {
            return entries.find { it.name == value }
        }
    }
}

/**
 * Income sources supported by the app.
 * Used for income transactions.
 */
enum class IncomeSource {
    SALARY,
    FREELANCE,
    INVESTMENTS,
    REFUND_RETURN,
    OTHER;

    companion object {
        fun fromString(value: String): IncomeSource? {
            return entries.find { it.name == value }
        }
    }
}

/**
 * Frequency options for recurring transactions.
 * Used to determine when to create the next transaction.
 */
enum class RecurringFrequency {
    DAILY,
    WEEKLY,
    MONTHLY;

    companion object {
        fun fromString(value: String): RecurringFrequency? {
            return entries.find { it.name == value }
        }
    }
}

/**
 * Transaction types for distinguishing between expenses and income.
 * Used in recurring transactions and tag associations.
 */
enum class TransactionType {
    EXPENSE,
    INCOME;

    companion object {
        fun fromString(value: String): TransactionType? {
            return entries.find { it.name == value }
        }
    }
}
