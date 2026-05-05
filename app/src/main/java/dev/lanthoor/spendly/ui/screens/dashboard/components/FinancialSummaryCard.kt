package dev.lanthoor.spendly.ui.screens.dashboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.lanthoor.spendly.R
import dev.lanthoor.spendly.ui.screens.dashboard.FinancialSummary
import dev.lanthoor.spendly.ui.theme.balanceColor
import dev.lanthoor.spendly.ui.theme.expenseColor
import dev.lanthoor.spendly.ui.theme.incomeColor
import dev.lanthoor.spendly.utils.CurrencyUtils
import dev.lanthoor.spendly.core.model.preferences.YearType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Card displaying financial summary with both month and YTD metrics.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialSummaryCard(
    summary: FinancialSummary,
    onYearTypeChange: (YearType) -> Unit,
    modifier: Modifier = Modifier
) {
    val monthLabel = remember {
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(
            Date(Calendar.getInstance().apply {
                set(Calendar.YEAR, summary.selectedYear)
                set(Calendar.MONTH, summary.selectedMonth - 1)
            }.timeInMillis)
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.title_financial_summary),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = monthLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Year type segmented button
                SingleChoiceSegmentedButtonRow {
                    SegmentedButton(
                        selected = summary.yearType == YearType.CALENDAR,
                        onClick = { onYearTypeChange(YearType.CALENDAR) },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        icon = {}
                    ) {
                        Text(stringResource(R.string.label_calendar_short))
                    }
                    SegmentedButton(
                        selected = summary.yearType == YearType.FINANCIAL,
                        onClick = { onYearTypeChange(YearType.FINANCIAL) },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        icon = {}
                    ) {
                        Text(stringResource(R.string.label_financial_year_short))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Two-column layout (YTD | Month)
            Row(modifier = Modifier.fillMaxWidth()) {
                // Left: Full Year metrics
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.label_full_year),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    FinancialMetricColumn(
                        income = summary.ytdIncome,
                        expenses = summary.ytdExpenses,
                        balance = summary.ytdNetBalance
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))
                VerticalDivider()
                Spacer(modifier = Modifier.width(8.dp))

                // Right: Selected Month metrics
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.tp_this_month),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    FinancialMetricColumn(
                        income = summary.monthIncome,
                        expenses = summary.monthExpenses,
                        balance = summary.monthNetBalance
                    )
                }
            }
        }
    }
}

/**
 * Column displaying income, expenses, and balance metrics.
 */
@Composable
private fun FinancialMetricColumn(
    income: Long,
    expenses: Long,
    balance: Long,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MetricRow(
            label = stringResource(R.string.label_income),
            amount = income,
            color = MaterialTheme.colorScheme.incomeColor()
        )
        MetricRow(
            label = stringResource(R.string.label_expenses),
            amount = expenses,
            color = MaterialTheme.colorScheme.expenseColor()
        )
        MetricRow(
            label = stringResource(R.string.label_balance),
            amount = balance,
            color = MaterialTheme.colorScheme.balanceColor(positive = balance >= 0)
        )
    }
}

/**
 * Single metric row showing label and amount.
 */
@Composable
private fun MetricRow(
    label: String,
    amount: Long,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = CurrencyUtils.formatPaise(amount),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
