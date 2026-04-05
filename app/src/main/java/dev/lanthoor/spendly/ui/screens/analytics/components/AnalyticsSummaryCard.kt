package dev.lanthoor.spendly.ui.screens.analytics.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.TrendDown
import com.adamglin.phosphoricons.regular.TrendUp
import dev.lanthoor.spendly.R
import dev.lanthoor.spendly.ui.theme.balanceColor
import dev.lanthoor.spendly.ui.theme.expenseColor
import dev.lanthoor.spendly.ui.theme.incomeColor
import dev.lanthoor.spendly.utils.CurrencyUtils
import dev.lanthoor.spendly.core.model.preferences.TimePeriod

/**
 * Card displaying period summary with income, expenses, and net balance
 * compared to the previous period.
 */
@Composable
fun AnalyticsSummaryCard(
    period: TimePeriod,
    income: Long,
    expense: Long,
    netBalance: Long,
    incomeChange: Float,
    expenseChange: Float,
    balanceChange: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Text(
                text = stringResource(R.string.title_period_summary),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Comparison period label
            Text(
                text = getComparisonLabel(period),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            // Metrics
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricRow(
                    label = stringResource(R.string.label_income),
                    amount = income,
                    change = incomeChange,
                    color = MaterialTheme.colorScheme.incomeColor(),
                    isExpenseMetric = false
                )

                MetricRow(
                    label = stringResource(R.string.label_expenses),
                    amount = expense,
                    change = expenseChange,
                    color = MaterialTheme.colorScheme.expenseColor(),
                    isExpenseMetric = true
                )

                MetricRow(
                    label = stringResource(R.string.label_net_balance),
                    amount = netBalance,
                    change = balanceChange,
                    color = MaterialTheme.colorScheme.balanceColor(positive = netBalance >= 0),
                    isExpenseMetric = false
                )
            }
        }
    }
}

/**
 * Single metric row with amount and percentage change indicator.
 */
@Composable
private fun MetricRow(
    label: String,
    amount: Long,
    change: Float,
    color: Color,
    isExpenseMetric: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Label and amount
        Column {
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

        Spacer(modifier = Modifier.width(16.dp))

        // Right: Change indicator
        ChangeIndicator(
            change = change,
            isExpenseMetric = isExpenseMetric
        )
    }
}

/**
 * Change indicator showing percentage change with trend icon.
 * For expenses: decrease (negative %) is good (green), increase is bad (red)
 * For income/balance: increase is good (green), decrease is bad (red)
 */
@Composable
private fun ChangeIndicator(
    change: Float,
    isExpenseMetric: Boolean,
    modifier: Modifier = Modifier
) {
    val isPositiveChange = change >= 0
    val absChange = kotlin.math.abs(change)

    // For expenses, invert the good/bad logic
    val isGoodChange = if (isExpenseMetric) {
        !isPositiveChange // Decrease in expenses is good
    } else {
        isPositiveChange // Increase in income/balance is good
    }

    val changeColor = if (isGoodChange) {
        MaterialTheme.colorScheme.incomeColor() // Green for good
    } else {
        MaterialTheme.colorScheme.expenseColor() // Red for bad
    }

    val icon = if (isPositiveChange) {
        PhosphorIcons.Regular.TrendUp
    } else {
        PhosphorIcons.Regular.TrendDown
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
        modifier = modifier
    ) {
        Icon(
            imageVector = icon,
            contentDescription = if (isPositiveChange) "Increase" else "Decrease",
            tint = changeColor,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = String.format("%.1f%%", absChange),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = changeColor
        )
    }
}

/**
 * Get the comparison period label based on the current period.
 */
@Composable
private fun getComparisonLabel(period: TimePeriod): String {
    return when (period) {
        is TimePeriod.ThisMonth -> stringResource(R.string.label_vs_last_month)
        is TimePeriod.LastMonth -> stringResource(R.string.label_vs_month_before_last)
        is TimePeriod.Last3Months -> stringResource(R.string.label_vs_prev_3_months)
        is TimePeriod.Last6Months -> stringResource(R.string.label_vs_prev_6_months)
        is TimePeriod.ThisYear -> stringResource(R.string.label_vs_last_year)
        is TimePeriod.ThisFinancialYear -> stringResource(R.string.label_vs_last_financial_year)
        is TimePeriod.LastYear -> stringResource(R.string.label_vs_year_before_last)
        is TimePeriod.Custom -> stringResource(R.string.label_vs_previous_period)
    }
}
