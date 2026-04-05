package dev.lanthoor.spendly.ui.screens.analytics

import dev.lanthoor.spendly.core.model.preferences.TimePeriod
import dev.lanthoor.spendly.core.model.preferences.getDateRange
import java.util.Calendar

object AnalyticsPeriodRangeCalculator {
    fun getPreviousPeriodRange(period: TimePeriod): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        val (currentStart, currentEnd) = period.getDateRange()

        return when (period) {
            is TimePeriod.ThisMonth -> {
                calendar.timeInMillis = currentStart
                calendar.add(Calendar.MONTH, -1)
                val start = calendar.timeInMillis
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                val end = calendar.timeInMillis
                start to end
            }

            is TimePeriod.LastMonth -> {
                calendar.timeInMillis = currentStart
                calendar.add(Calendar.MONTH, -1)
                val start = calendar.timeInMillis
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                val end = calendar.timeInMillis
                start to end
            }

            is TimePeriod.Last3Months -> {
                calendar.timeInMillis = currentStart
                calendar.add(Calendar.MONTH, -3)
                val start = calendar.timeInMillis
                calendar.timeInMillis = currentEnd
                calendar.add(Calendar.MONTH, -3)
                val end = calendar.timeInMillis
                start to end
            }

            is TimePeriod.Last6Months -> {
                calendar.timeInMillis = currentStart
                calendar.add(Calendar.MONTH, -6)
                val start = calendar.timeInMillis
                calendar.timeInMillis = currentEnd
                calendar.add(Calendar.MONTH, -6)
                val end = calendar.timeInMillis
                start to end
            }

            is TimePeriod.ThisYear -> {
                calendar.timeInMillis = currentStart
                calendar.add(Calendar.YEAR, -1)
                val start = calendar.timeInMillis
                calendar.timeInMillis = currentEnd
                calendar.add(Calendar.YEAR, -1)
                val end = calendar.timeInMillis
                start to end
            }

            is TimePeriod.ThisFinancialYear -> {
                calendar.timeInMillis = currentStart
                calendar.add(Calendar.YEAR, -1)
                val start = calendar.timeInMillis
                calendar.timeInMillis = currentEnd
                calendar.add(Calendar.YEAR, -1)
                val end = calendar.timeInMillis
                start to end
            }

            is TimePeriod.LastYear -> {
                calendar.timeInMillis = currentStart
                calendar.add(Calendar.YEAR, -1)
                val start = calendar.timeInMillis
                calendar.timeInMillis = currentEnd
                calendar.add(Calendar.YEAR, -1)
                val end = calendar.timeInMillis
                start to end
            }

            is TimePeriod.Custom -> {
                val periodLength = currentEnd - currentStart
                val start = currentStart - periodLength
                val end = currentEnd - periodLength
                start to end
            }
        }
    }
}
