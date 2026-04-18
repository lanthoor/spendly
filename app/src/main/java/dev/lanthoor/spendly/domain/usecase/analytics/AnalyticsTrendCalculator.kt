package dev.lanthoor.spendly.domain.usecase.analytics

import dev.lanthoor.spendly.domain.model.Expense
import dev.lanthoor.spendly.domain.model.Income
import dev.lanthoor.spendly.domain.model.LineChartEntry
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object AnalyticsTrendCalculator {
    fun aggregateExpensesByMonth(expenses: List<Expense>): List<LineChartEntry> {
        val monthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val labelFormat = SimpleDateFormat("MMM", Locale.getDefault())
        val calendar = Calendar.getInstance()

        val monthlyTotals = expenses.groupBy { expense ->
            calendar.timeInMillis = expense.date
            monthFormat.format(calendar.time)
        }.mapValues { (_, expenseList) ->
            expenseList.sumOf { it.amount }
        }

        return monthlyTotals.map { (monthStr, amount) ->
            val parts = monthStr.split("-")
            calendar.set(Calendar.YEAR, parts[0].toInt())
            calendar.set(Calendar.MONTH, parts[1].toInt() - 1)
            calendar.set(Calendar.DAY_OF_MONTH, 1)

            LineChartEntry(
                date = monthStr,
                dateLabel = labelFormat.format(calendar.time),
                amount = amount,
                timestamp = calendar.timeInMillis
            )
        }.sortedBy { it.timestamp }
    }

    fun aggregateIncomesByMonth(incomes: List<Income>): List<LineChartEntry> {
        val monthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val labelFormat = SimpleDateFormat("MMM", Locale.getDefault())
        val calendar = Calendar.getInstance()

        val monthlyTotals = incomes.groupBy { income ->
            calendar.timeInMillis = income.date
            monthFormat.format(calendar.time)
        }.mapValues { (_, incomeList) ->
            incomeList.sumOf { it.amount }
        }

        return monthlyTotals.map { (monthStr, amount) ->
            val parts = monthStr.split("-")
            calendar.set(Calendar.YEAR, parts[0].toInt())
            calendar.set(Calendar.MONTH, parts[1].toInt() - 1)
            calendar.set(Calendar.DAY_OF_MONTH, 1)

            LineChartEntry(
                date = monthStr,
                dateLabel = labelFormat.format(calendar.time),
                amount = amount,
                timestamp = calendar.timeInMillis
            )
        }.sortedBy { it.timestamp }
    }

    fun calculateNetWorthData(
        incomeData: List<LineChartEntry>,
        expenseData: List<LineChartEntry>
    ): List<LineChartEntry> {
        if (incomeData.isEmpty() && expenseData.isEmpty()) return emptyList()

        val incomeMap = incomeData.associateBy { it.date }
        val expenseMap = expenseData.associateBy { it.date }

        val allDates = (incomeMap.keys + expenseMap.keys).toSet()
            .map { date ->
                val timestamp = incomeMap[date]?.timestamp ?: expenseMap[date]?.timestamp ?: 0L
                date to timestamp
            }
            .sortedBy { it.second }

        val result = mutableListOf<LineChartEntry>()
        var cumulativeNetWorth = 0L

        allDates.forEach { (date, timestamp) ->
            val incomeAmount = incomeMap[date]?.amount ?: 0L
            val expenseAmount = expenseMap[date]?.amount ?: 0L
            cumulativeNetWorth += (incomeAmount - expenseAmount)
            val dateLabel = incomeMap[date]?.dateLabel ?: expenseMap[date]?.dateLabel ?: date

            result.add(
                LineChartEntry(
                    date = date,
                    dateLabel = dateLabel,
                    amount = cumulativeNetWorth,
                    timestamp = timestamp
                )
            )
        }

        return result
    }
}
