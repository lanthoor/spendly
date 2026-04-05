package dev.lanthoor.spendly.ui.screens.analytics.components.canvas

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import dev.lanthoor.spendly.R
import dev.lanthoor.spendly.domain.model.LineChartEntry
import dev.lanthoor.spendly.utils.CurrencyUtils

@Composable
internal fun rememberLineChartAccessibilityDescription(
    incomeData: List<LineChartEntry>,
    expenseData: List<LineChartEntry>,
    netWorthData: List<LineChartEntry>,
    selectedPointIndex: Int?,
    selectedDataType: String?
): String {
    val chartIntro = stringResource(R.string.analytics_chart_intro)
    val incomeLabel = stringResource(R.string.label_income)
    val expenseLabel = stringResource(R.string.label_expense)
    val netWorthLabel = stringResource(R.string.label_net_worth)
    val tapDetails = stringResource(R.string.msg_tap_point_for_details)
    val currentlySelectedLabel = stringResource(R.string.msg_currently_selected)

    return remember(
        incomeData,
        expenseData,
        netWorthData,
        selectedPointIndex,
        selectedDataType,
        chartIntro,
        incomeLabel,
        expenseLabel,
        netWorthLabel,
        tapDetails,
        currentlySelectedLabel
    ) {
        buildString {
            append(chartIntro)
            if (incomeData.isNotEmpty()) {
                append(" $incomeLabel data: ${incomeData.size} points. ")
            }
            if (expenseData.isNotEmpty()) {
                append(" $expenseLabel data: ${expenseData.size} points. ")
            }
            if (netWorthData.isNotEmpty()) {
                append(" $netWorthLabel data: ${netWorthData.size} points. ")
            }
            val index = selectedPointIndex
            val dataType = selectedDataType
            if (index != null && dataType != null) {
                val point = when (dataType) {
                    "income" -> if (index < incomeData.size) incomeData[index] else null
                    "expense" -> if (index < expenseData.size) expenseData[index] else null
                    "networth" -> if (index < netWorthData.size) netWorthData[index] else null
                    else -> null
                }
                if (point != null) {
                    append(" $currentlySelectedLabel ${point.dateLabel}, ")
                    append("${CurrencyUtils.formatPaise(point.amount)}.")
                }
            } else {
                append(" $tapDetails")
            }
        }
    }
}
