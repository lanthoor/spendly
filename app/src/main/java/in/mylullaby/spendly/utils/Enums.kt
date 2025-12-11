package `in`.mylullaby.spendly.utils

/**
 * Payment methods supported by the app.
 * Used for expense transactions.
 *
 * @deprecated Replaced by Account system. Use AccountType instead.
 */
@Deprecated("Replaced by Account system. Use AccountType instead.", ReplaceWith("AccountType"))
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

        /**
         * Safe parsing with fallback to default value.
         * Prevents IllegalArgumentException crashes from invalid database values.
         * Logs warning when unknown value is encountered.
         */
        fun fromStringOrDefault(value: String, default: PaymentMethod = CASH): PaymentMethod {
            return fromString(value) ?: run {
                android.util.Log.w("PaymentMethod", "Unknown payment method: $value, defaulting to ${default.name}")
                default
            }
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

        /**
         * Safe parsing with fallback to default value.
         * Prevents IllegalArgumentException crashes from invalid database values.
         * Logs warning when unknown value is encountered.
         */
        fun fromStringOrDefault(value: String, default: IncomeSource = OTHER): IncomeSource {
            return fromString(value) ?: run {
                android.util.Log.w("IncomeSource", "Unknown income source: $value, defaulting to ${default.name}")
                default
            }
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

        /**
         * Safe parsing with fallback to default value.
         * Prevents IllegalArgumentException crashes from invalid database values.
         * Logs warning when unknown value is encountered.
         */
        fun fromStringOrDefault(value: String, default: RecurringFrequency = MONTHLY): RecurringFrequency {
            return fromString(value) ?: run {
                android.util.Log.w("RecurringFrequency", "Unknown recurring frequency: $value, defaulting to ${default.name}")
                default
            }
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

        /**
         * Safe parsing with fallback to default value.
         * Prevents IllegalArgumentException crashes from invalid database values.
         * Logs warning when unknown value is encountered.
         */
        fun fromStringOrDefault(value: String, default: TransactionType = EXPENSE): TransactionType {
            return fromString(value) ?: run {
                android.util.Log.w("TransactionType", "Unknown transaction type: $value, defaulting to ${default.name}")
                default
            }
        }
    }
}

/**
 * Account types supported by the app.
 * Used to categorize different financial accounts users can create.
 *
 * Types:
 * - BANK: Savings/checking bank accounts
 * - CARD: Credit/debit cards
 * - WALLET: Digital wallets (PayTM, GPay, PhonePe, etc.)
 * - CASH: Physical cash
 * - LOAN: Borrowed money accounts
 * - INVESTMENT: Investment/brokerage accounts
 */
enum class AccountType {
    BANK,
    CARD,
    WALLET,
    CASH,
    LOAN,
    INVESTMENT;

    companion object {
        fun fromString(value: String): AccountType? {
            return entries.find { it.name == value }
        }

        /**
         * Safe parsing with fallback to default value.
         * Prevents IllegalArgumentException crashes from invalid database values.
         * Logs warning when unknown value is encountered.
         */
        fun fromStringOrDefault(value: String, default: AccountType = BANK): AccountType {
            return fromString(value) ?: run {
                android.util.Log.w("AccountType", "Unknown account type: $value, defaulting to ${default.name}")
                default
            }
        }
    }
}

/**
 * Convert AccountType enum to display string in title case.
 *
 * Examples:
 * - BANK -> "Bank"
 * - CARD -> "Card"
 * - WALLET -> "Wallet"
 * - CASH -> "Cash"
 * - LOAN -> "Loan"
 * - INVESTMENT -> "Investment"
 */
fun AccountType.toDisplayName(): String {
    return name.lowercase()
        .split('_')
        .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
}

/**
 * Get the default Phosphor icon name for each account type.
 *
 * Returns:
 * - BANK -> "bank"
 * - CARD -> "creditcard"
 * - WALLET -> "wallet"
 * - CASH -> "money"
 * - LOAN -> "receipt"
 * - INVESTMENT -> "trendingup"
 */
fun AccountType.getDefaultIcon(): String {
    return when (this) {
        AccountType.BANK -> "bank"
        AccountType.CARD -> "creditcard"
        AccountType.WALLET -> "wallet"
        AccountType.CASH -> "money"
        AccountType.LOAN -> "receipt"
        AccountType.INVESTMENT -> "trendingup"
    }
}
