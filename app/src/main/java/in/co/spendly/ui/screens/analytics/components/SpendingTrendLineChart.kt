package `in`.co.spendly.ui.screens.analytics.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.co.spendly.R
import `in`.co.spendly.domain.model.LineChartEntry
import `in`.co.spendly.ui.screens.analytics.components.canvas.CustomLineChart

/**
 * Spending trend line chart with custom Canvas implementation.
 * Shows income (green), expense (red), and net worth (blue) trends over time.
 * Tap on a data point to see daily/monthly summary in a popover.
 * Displays daily trends for current month, monthly trends for FY/Calendar year.
 * Features: grid lines, Y-axis labels with currency, X-axis labels with smart spacing.
 */
@Composable
fun SpendingTrendLineChart(
    incomeData: List<LineChartEntry>,
    expenseData: List<LineChartEntry>,
    netWorthData: List<LineChartEntry>,
    periodLabel: String,
    modifier: Modifier = Modifier
) {
    val incomeColor = Color(0xFF4CAF50) // Green for income
    val expenseColor = Color(0xFFF44336) // Red for expense
    val netWorthColor = Color(0xFF2196F3) // Blue for net worth

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
            // Header with legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.label_spending_trend),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Legend
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Income legend
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.foundation.Canvas(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                        ) {
                            drawCircle(color = incomeColor)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.label_income),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Expense legend
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.foundation.Canvas(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                        ) {
                            drawCircle(color = expenseColor)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.label_expense),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Net Worth legend
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.foundation.Canvas(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                        ) {
                            drawCircle(color = netWorthColor)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.label_net_worth),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp)
            )

            // Empty state
            if (incomeData.isEmpty() && expenseData.isEmpty() && netWorthData.isEmpty()) {
                Text(
                    text = "No transaction data available for $periodLabel",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                // Custom Canvas line chart with tap interaction
                CustomLineChart(
                    incomeData = incomeData,
                    expenseData = expenseData,
                    netWorthData = netWorthData
                )
            }
        }
    }
}
