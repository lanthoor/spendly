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
 * Convert PaymentMethod enum to display string in title case with proper spacing.
 * Keeps acronyms in uppercase.
 *
 * Examples:
 * - CASH -> "Cash"
 * - UPI -> "UPI"
 * - DEBIT_CARD -> "Debit Card"
 * - NET_BANKING -> "Net Banking"
 */
fun PaymentMethod.toDisplayName(): String {
    return when (this) {
        PaymentMethod.UPI -> "UPI"
        else -> name.lowercase()
            .replace('_', ' ')
            .split(' ')
            .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
    }
}

/**
 * Income sources supported by the app.
 * Used for income transactions.
 */
enum class IncomeSource {
    SALARY,
    FREELANCE,
    INVESTMENT,
    GIFTS,
    REFUND,
    BUSINESS,
    RENTAL,
    INTEREST,
    OTHER;

    companion object {
        fun fromString(value: String): IncomeSource? {
            return entries.find { it.name == value }
        }
    }
}

/**
 * Convert IncomeSource enum to display string in title case.
 *
 * Examples:
 * - SALARY -> "Salary"
 * - FREELANCE -> "Freelance"
 * - INVESTMENT -> "Investment"
 */
fun IncomeSource.toDisplayString(): String {
    return name.lowercase()
        .split('_')
        .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
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
