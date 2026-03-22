package dev.lanthoor.spendly.utils

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
    BONUS,
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
                EnumLoggingUtil.logUnknownEnum("IncomeSource", value, default.name)
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
        fun fromStringOrDefault(
            value: String,
            default: RecurringFrequency = MONTHLY
        ): RecurringFrequency {
            return fromString(value) ?: run {
                EnumLoggingUtil.logUnknownEnum("RecurringFrequency", value, default.name)
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
        fun fromStringOrDefault(
            value: String,
            default: TransactionType = EXPENSE
        ): TransactionType {
            return fromString(value) ?: run {
                EnumLoggingUtil.logUnknownEnum("TransactionType", value, default.name)
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
                EnumLoggingUtil.logUnknownEnum("AccountType", value, default.name)
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

/**
 * Filter type for distinguishing between expense and income filters.
 * Used in FilterBottomSheet to determine which fields to show.
 */
enum class FilterType {
    EXPENSE,
    INCOME
}

/**
 * App theme options supported by the app.
 * Used to control light/dark mode throughout the application.
 *
 * Types:
 * - LIGHT: Always use light theme
 * - DARK: Always use dark theme
 * - SYSTEM: Follow system theme preference
 */
enum class AppTheme {
    LIGHT,
    DARK,
    SYSTEM;

    companion object {
        fun fromString(value: String): AppTheme? {
            return entries.find { it.name == value }
        }

        /**
         * Safe parsing with fallback to default value.
         * Prevents IllegalArgumentException crashes from invalid DataStore values.
         * Logs warning when unknown value is encountered.
         */
        fun fromStringOrDefault(value: String, default: AppTheme = SYSTEM): AppTheme {
            return fromString(value) ?: run {
                EnumLoggingUtil.logUnknownEnum("AppTheme", value, default.name)
                default
            }
        }
    }
}

/**
 * Convert AppTheme enum to display string in title case.
 *
 * Examples:
 * - LIGHT -> "Light"
 * - DARK -> "Dark"
 * - SYSTEM -> "System"
 */
fun AppTheme.toDisplayName(): String {
    return name.lowercase()
        .split('_')
        .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
}

/**
 * App language preference.
 * Determines which locale is used for displaying strings and resources.
 *
 * Languages:
 * - ENGLISH: English (en)
 * - HINDI: Hindi (hi)
 * - MALAYALAM: Malayalam (ml)
 */
enum class AppLanguage(val code: String, val displayNameRes: Int) {
    ENGLISH("en", dev.lanthoor.spendly.R.string.language_english),
    HINDI("hi", dev.lanthoor.spendly.R.string.language_hindi),
    MALAYALAM("ml", dev.lanthoor.spendly.R.string.language_malayalam);

    companion object {
        fun fromString(value: String): AppLanguage? {
            return entries.find { it.name == value }
        }

        /**
         * Safe parsing with fallback to default value.
         * Prevents IllegalArgumentException crashes from invalid DataStore values.
         * Logs warning when unknown value is encountered.
         */
        fun fromStringOrDefault(value: String, default: AppLanguage = ENGLISH): AppLanguage {
            return fromString(value) ?: run {
                EnumLoggingUtil.logUnknownEnum("AppLanguage", value, default.name)
                default
            }
        }

        /**
         * Get language from locale code (e.g., "en", "hi", "ml")
         */
        fun fromLocaleCode(code: String): AppLanguage? {
            return entries.find { it.code == code }
        }
    }
}

/**
 * Year type for financial calculations.
 * Determines the start month for year-to-date calculations.
 *
 * Types:
 * - CALENDAR: January to December (Jan-Dec)
 * - FINANCIAL: April to March (Apr-Mar, Indian Financial Year)
 */
enum class YearType {
    CALENDAR,
    FINANCIAL;

    companion object {
        fun fromString(value: String): YearType? {
            return entries.find { it.name == value }
        }

        /**
         * Safe parsing with fallback to default value.
         * Prevents IllegalArgumentException crashes from invalid DataStore values.
         * Logs warning when unknown value is encountered.
         */
        fun fromStringOrDefault(value: String, default: YearType = CALENDAR): YearType {
            return fromString(value) ?: run {
                EnumLoggingUtil.logUnknownEnum("YearType", value, default.name)
                default
            }
        }
    }

    /**
     * Get the year start (year, month) based on selected month/year.
     * Month is 1-indexed (1=Jan, 12=Dec).
     */
    fun getYearStart(selectedYear: Int, selectedMonth: Int): Pair<Int, Int> {
        return when (this) {
            CALENDAR -> Pair(selectedYear, 1) // January 1
            FINANCIAL -> {
                // Indian FY: Apr-Mar
                if (selectedMonth >= 4) {
                    Pair(selectedYear, 4) // April 1 of same year
                } else {
                    Pair(selectedYear - 1, 4) // April 1 of previous year
                }
            }
        }
    }
}

/**
 * Convert YearType enum to display name.
 */
fun YearType.toDisplayName(): String {
    return when (this) {
        YearType.CALENDAR -> "Calendar Year"
        YearType.FINANCIAL -> "Financial Year"
    }
}

/**
 * Get the display range string for year type.
 */
fun YearType.getDisplayRange(): String {
    return when (this) {
        YearType.CALENDAR -> "January - December"
        YearType.FINANCIAL -> "April - March"
    }
}

/**
 * Time period options for analytics and charts.
 * Determines the date range for displaying analytics data.
 *
 * Types:
 * - THIS_MONTH: Current month (from 1st to last day)
 * - LAST_MONTH: Previous month
 * - LAST_3_MONTHS: Last 3 complete months
 * - LAST_6_MONTHS: Last 6 complete months
 * - THIS_YEAR: Current calendar year (Jan 1 to Dec 31)
 * - LAST_YEAR: Previous calendar year
 * - CUSTOM: User-defined date range with start and end timestamps
 */
sealed class TimePeriod {
    data object ThisMonth : TimePeriod()
    data object LastMonth : TimePeriod()
    data object Last3Months : TimePeriod()
    data object Last6Months : TimePeriod()
    data object ThisYear : TimePeriod()
    data object ThisFinancialYear : TimePeriod()
    data object LastYear : TimePeriod()
    data class Custom(val startDate: Long, val endDate: Long) : TimePeriod()

    companion object {
        /**
         * Parse TimePeriod from string representation.
         * Used for persisting/restoring from DataStore.
         *
         * Format:
         * - Simple types: "THIS_MONTH", "LAST_MONTH", etc.
         * - Custom: "CUSTOM:startTimestamp:endTimestamp"
         */
        fun fromString(value: String): TimePeriod? {
            return when {
                value == "THIS_MONTH" -> ThisMonth
                value == "LAST_MONTH" -> LastMonth
                value == "LAST_3_MONTHS" -> Last3Months
                value == "LAST_6_MONTHS" -> Last6Months
                value == "THIS_YEAR" -> ThisYear
                value == "THIS_FINANCIAL_YEAR" -> ThisFinancialYear
                value == "LAST_YEAR" -> LastYear
                value.startsWith("CUSTOM:") -> {
                    val parts = value.split(":")
                    if (parts.size == 3) {
                        val startDate = parts[1].toLongOrNull()
                        val endDate = parts[2].toLongOrNull()
                        if (startDate != null && endDate != null) {
                            Custom(startDate, endDate)
                        } else null
                    } else null
                }

                else -> null
            }
        }

        /**
         * Safe parsing with fallback to default value.
         * Prevents crashes from invalid DataStore values.
         * Logs warning when unknown value is encountered.
         */
        fun fromStringOrDefault(value: String, default: TimePeriod = ThisMonth): TimePeriod {
            return fromString(value) ?: run {
                EnumLoggingUtil.logUnknownEnum("TimePeriod", value, default.toString())
                default
            }
        }
    }

    /**
     * Convert TimePeriod to string representation for persistence.
     */
    override fun toString(): String {
        return when (this) {
            is ThisMonth -> "THIS_MONTH"
            is LastMonth -> "LAST_MONTH"
            is Last3Months -> "LAST_3_MONTHS"
            is Last6Months -> "LAST_6_MONTHS"
            is ThisYear -> "THIS_YEAR"
            is ThisFinancialYear -> "THIS_FINANCIAL_YEAR"
            is LastYear -> "LAST_YEAR"
            is Custom -> "CUSTOM:$startDate:$endDate"
        }
    }
}

/**
 * Convert TimePeriod to display name.
 */
fun TimePeriod.toDisplayName(): String {
    return when (this) {
        is TimePeriod.ThisMonth -> "This Month"
        is TimePeriod.LastMonth -> "Last Month"
        is TimePeriod.Last3Months -> "Last 3 Months"
        is TimePeriod.Last6Months -> "Last 6 Months"
        is TimePeriod.ThisYear -> "This Year"
        is TimePeriod.ThisFinancialYear -> "This Financial Year"
        is TimePeriod.LastYear -> "Last Year"
        is TimePeriod.Custom -> {
            // Format custom date range
            val startCal = java.util.Calendar.getInstance().apply { timeInMillis = startDate }
            val endCal = java.util.Calendar.getInstance().apply { timeInMillis = endDate }
            val dateFormat =
                java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
            "${dateFormat.format(startCal.time)} - ${dateFormat.format(endCal.time)}"
        }
    }
}

/**
 * Get date range (startTimestamp, endTimestamp) for the time period.
 * Both timestamps are in milliseconds since epoch.
 */
fun TimePeriod.getDateRange(): Pair<Long, Long> {
    val calendar = java.util.Calendar.getInstance()
    val now = System.currentTimeMillis()

    return when (this) {
        is TimePeriod.ThisMonth -> {
            calendar.timeInMillis = now
            calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
            calendar.set(java.util.Calendar.MINUTE, 0)
            calendar.set(java.util.Calendar.SECOND, 0)
            calendar.set(java.util.Calendar.MILLISECOND, 0)
            val start = calendar.timeInMillis

            calendar.set(
                java.util.Calendar.DAY_OF_MONTH,
                calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
            )
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
            calendar.set(java.util.Calendar.MINUTE, 59)
            calendar.set(java.util.Calendar.SECOND, 59)
            calendar.set(java.util.Calendar.MILLISECOND, 999)
            val end = calendar.timeInMillis

            Pair(start, end)
        }

        is TimePeriod.LastMonth -> {
            calendar.timeInMillis = now
            calendar.add(java.util.Calendar.MONTH, -1)
            calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
            calendar.set(java.util.Calendar.MINUTE, 0)
            calendar.set(java.util.Calendar.SECOND, 0)
            calendar.set(java.util.Calendar.MILLISECOND, 0)
            val start = calendar.timeInMillis

            calendar.set(
                java.util.Calendar.DAY_OF_MONTH,
                calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
            )
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
            calendar.set(java.util.Calendar.MINUTE, 59)
            calendar.set(java.util.Calendar.SECOND, 59)
            calendar.set(java.util.Calendar.MILLISECOND, 999)
            val end = calendar.timeInMillis

            Pair(start, end)
        }

        is TimePeriod.Last3Months -> {
            calendar.timeInMillis = now
            calendar.add(java.util.Calendar.MONTH, -3)
            calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
            calendar.set(java.util.Calendar.MINUTE, 0)
            calendar.set(java.util.Calendar.SECOND, 0)
            calendar.set(java.util.Calendar.MILLISECOND, 0)
            val start = calendar.timeInMillis

            calendar.timeInMillis = now
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
            calendar.set(java.util.Calendar.MINUTE, 59)
            calendar.set(java.util.Calendar.SECOND, 59)
            calendar.set(java.util.Calendar.MILLISECOND, 999)
            val end = calendar.timeInMillis

            Pair(start, end)
        }

        is TimePeriod.Last6Months -> {
            calendar.timeInMillis = now
            calendar.add(java.util.Calendar.MONTH, -6)
            calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
            calendar.set(java.util.Calendar.MINUTE, 0)
            calendar.set(java.util.Calendar.SECOND, 0)
            calendar.set(java.util.Calendar.MILLISECOND, 0)
            val start = calendar.timeInMillis

            calendar.timeInMillis = now
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
            calendar.set(java.util.Calendar.MINUTE, 59)
            calendar.set(java.util.Calendar.SECOND, 59)
            calendar.set(java.util.Calendar.MILLISECOND, 999)
            val end = calendar.timeInMillis

            Pair(start, end)
        }

        is TimePeriod.ThisYear -> {
            calendar.timeInMillis = now
            calendar.set(java.util.Calendar.MONTH, java.util.Calendar.JANUARY)
            calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
            calendar.set(java.util.Calendar.MINUTE, 0)
            calendar.set(java.util.Calendar.SECOND, 0)
            calendar.set(java.util.Calendar.MILLISECOND, 0)
            val start = calendar.timeInMillis

            calendar.set(java.util.Calendar.MONTH, java.util.Calendar.DECEMBER)
            calendar.set(java.util.Calendar.DAY_OF_MONTH, 31)
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
            calendar.set(java.util.Calendar.MINUTE, 59)
            calendar.set(java.util.Calendar.SECOND, 59)
            calendar.set(java.util.Calendar.MILLISECOND, 999)
            val end = calendar.timeInMillis

            Pair(start, end)
        }

        is TimePeriod.ThisFinancialYear -> {
            // Financial year in India: April 1 to March 31
            calendar.timeInMillis = now
            val currentMonth = calendar.get(java.util.Calendar.MONTH)
            val currentYear = calendar.get(java.util.Calendar.YEAR)

            // If current month is Jan-Mar, FY started last year
            val fyStartYear = if (currentMonth < java.util.Calendar.APRIL) {
                currentYear - 1
            } else {
                currentYear
            }

            // FY start: April 1
            calendar.set(fyStartYear, java.util.Calendar.APRIL, 1, 0, 0, 0)
            calendar.set(java.util.Calendar.MILLISECOND, 0)
            val start = calendar.timeInMillis

            // FY end: March 31 next year
            calendar.set(fyStartYear + 1, java.util.Calendar.MARCH, 31, 23, 59, 59)
            calendar.set(java.util.Calendar.MILLISECOND, 999)
            val end = calendar.timeInMillis

            Pair(start, end)
        }

        is TimePeriod.LastYear -> {
            calendar.timeInMillis = now
            calendar.add(java.util.Calendar.YEAR, -1)
            calendar.set(java.util.Calendar.MONTH, java.util.Calendar.JANUARY)
            calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
            calendar.set(java.util.Calendar.MINUTE, 0)
            calendar.set(java.util.Calendar.SECOND, 0)
            calendar.set(java.util.Calendar.MILLISECOND, 0)
            val start = calendar.timeInMillis

            calendar.set(java.util.Calendar.MONTH, java.util.Calendar.DECEMBER)
            calendar.set(java.util.Calendar.DAY_OF_MONTH, 31)
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
            calendar.set(java.util.Calendar.MINUTE, 59)
            calendar.set(java.util.Calendar.SECOND, 59)
            calendar.set(java.util.Calendar.MILLISECOND, 999)
            val end = calendar.timeInMillis

            Pair(start, end)
        }

        is TimePeriod.Custom -> Pair(startDate, endDate)
    }
}
