package dev.lanthoor.spendly.core.model.preferences

import java.util.Calendar

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
                        if (startDate != null && endDate != null) Custom(startDate, endDate) else null
                    } else {
                        null
                    }
                }

                else -> null
            }
        }

        fun fromStringOrDefault(value: String, default: TimePeriod = ThisMonth): TimePeriod {
            return fromString(value) ?: default
        }
    }

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

fun TimePeriod.getDateRange(): Pair<Long, Long> {
    val calendar = Calendar.getInstance()
    val now = System.currentTimeMillis()

    return when (this) {
        is TimePeriod.ThisMonth -> {
            calendar.timeInMillis = now
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val start = calendar.timeInMillis

            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 999)
            Pair(start, calendar.timeInMillis)
        }

        is TimePeriod.LastMonth -> {
            calendar.timeInMillis = now
            calendar.add(Calendar.MONTH, -1)
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val start = calendar.timeInMillis

            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 999)
            Pair(start, calendar.timeInMillis)
        }

        is TimePeriod.Last3Months -> {
            calendar.timeInMillis = now
            calendar.add(Calendar.MONTH, -3)
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val start = calendar.timeInMillis

            calendar.timeInMillis = now
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 999)
            Pair(start, calendar.timeInMillis)
        }

        is TimePeriod.Last6Months -> {
            calendar.timeInMillis = now
            calendar.add(Calendar.MONTH, -6)
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val start = calendar.timeInMillis

            calendar.timeInMillis = now
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 999)
            Pair(start, calendar.timeInMillis)
        }

        is TimePeriod.ThisYear -> {
            calendar.timeInMillis = now
            calendar.set(Calendar.MONTH, Calendar.JANUARY)
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val start = calendar.timeInMillis

            calendar.set(Calendar.MONTH, Calendar.DECEMBER)
            calendar.set(Calendar.DAY_OF_MONTH, 31)
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 999)
            Pair(start, calendar.timeInMillis)
        }

        is TimePeriod.ThisFinancialYear -> {
            calendar.timeInMillis = now
            val currentMonth = calendar.get(Calendar.MONTH)
            val currentYear = calendar.get(Calendar.YEAR)
            val fyStartYear = if (currentMonth < Calendar.APRIL) currentYear - 1 else currentYear

            calendar.set(fyStartYear, Calendar.APRIL, 1, 0, 0, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val start = calendar.timeInMillis

            calendar.set(fyStartYear + 1, Calendar.MARCH, 31, 23, 59, 59)
            calendar.set(Calendar.MILLISECOND, 999)
            Pair(start, calendar.timeInMillis)
        }

        is TimePeriod.LastYear -> {
            calendar.timeInMillis = now
            calendar.add(Calendar.YEAR, -1)
            calendar.set(Calendar.MONTH, Calendar.JANUARY)
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val start = calendar.timeInMillis

            calendar.set(Calendar.MONTH, Calendar.DECEMBER)
            calendar.set(Calendar.DAY_OF_MONTH, 31)
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 999)
            Pair(start, calendar.timeInMillis)
        }

        is TimePeriod.Custom -> Pair(startDate, endDate)
    }
}
