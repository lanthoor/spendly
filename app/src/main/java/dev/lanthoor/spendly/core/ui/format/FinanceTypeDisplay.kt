package dev.lanthoor.spendly.core.ui.format

import dev.lanthoor.spendly.core.model.finance.AccountType
import dev.lanthoor.spendly.core.model.finance.IncomeSource
import dev.lanthoor.spendly.core.model.preferences.AppLanguage
import dev.lanthoor.spendly.core.ui.format.displayNameRes
import dev.lanthoor.spendly.core.model.preferences.AppTheme
import dev.lanthoor.spendly.core.model.preferences.LockTimeout
import dev.lanthoor.spendly.core.model.preferences.TimePeriod
import dev.lanthoor.spendly.core.model.preferences.YearType
import dev.lanthoor.spendly.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

fun IncomeSource.toDisplayString(): String {
    return name.lowercase()
        .split('_')
        .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
}

fun AccountType.toDisplayName(): String {
    return name.lowercase()
        .split('_')
        .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
}

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

fun AppTheme.toDisplayName(): String {
    return name.lowercase()
        .split('_')
        .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
}

val AppLanguage.displayNameRes: Int
    get() = when (this) {
        AppLanguage.ENGLISH -> R.string.language_english
        AppLanguage.HINDI -> R.string.language_hindi
        AppLanguage.MALAYALAM -> R.string.language_malayalam
    }

fun YearType.toDisplayName(): String {
    return when (this) {
        YearType.CALENDAR -> "Calendar Year"
        YearType.FINANCIAL -> "Financial Year"
    }
}

fun YearType.getDisplayRange(): String {
    return when (this) {
        YearType.CALENDAR -> "January - December"
        YearType.FINANCIAL -> "April - March"
    }
}

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
            val startCal = Calendar.getInstance().apply { timeInMillis = startDate }
            val endCal = Calendar.getInstance().apply { timeInMillis = endDate }
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            "${dateFormat.format(startCal.time)} - ${dateFormat.format(endCal.time)}"
        }
    }
}

fun LockTimeout.toDisplayName(): String {
    return when (this) {
        LockTimeout.IMMEDIATELY -> "Immediately"
        LockTimeout.ONE_MINUTE -> "After 1 minute"
        LockTimeout.FIVE_MINUTES -> "After 5 minutes"
        LockTimeout.FIFTEEN_MINUTES -> "After 15 minutes"
    }
}
