package `in`.mylullaby.spendly.ui.screens.dashboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.mylullaby.spendly.ui.screens.dashboard.FinancialSummary
import `in`.mylullaby.spendly.utils.CurrencyUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Card displaying financial summary with income, expenses, and net balance
 */
@Composable
fun FinancialSummaryCard(
    summary: FinancialSummary,
    modifier: Modifier = Modifier
) {
    val currentMonth = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with current month
            Column {
                Text(
                    text = "Financial Summary",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = currentMonth,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider()

            // Total Income
            FinancialMetricRow(
                label = "Total Income",
                amount = summary.totalIncome,
                amountColor = Color(0xFF2E7D32) // Green for income
            )

            // Total Expenses
            FinancialMetricRow(
                label = "Total Expenses",
                amount = summary.totalExpenses,
                amountColor = Color(0xFFC62828) // Red for expenses
            )

            HorizontalDivider()

            // Net Balance
            FinancialMetricRow(
                label = "Net Balance",
                amount = summary.netBalance,
                amountColor = if (summary.netBalance >= 0) Color(0xFF1976D2) else Color(0xFFC62828),
                isBold = true
            )
        }
    }
}

/**
 * Row displaying a single financial metric
 */
@Composable
private fun FinancialMetricRow(
    label: String,
    amount: Long,
    amountColor: Color,
    isBold: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isBold) FontWeight.SemiBold else FontWeight.Normal
        )

        // Amount
        Text(
            text = CurrencyUtils.formatPaise(amount),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
            color = amountColor
        )
    }
}
