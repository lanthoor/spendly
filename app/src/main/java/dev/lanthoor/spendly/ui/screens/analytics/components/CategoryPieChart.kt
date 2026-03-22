package dev.lanthoor.spendly.ui.screens.analytics.components

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.lanthoor.spendly.R
import dev.lanthoor.spendly.domain.model.PieChartEntry
import dev.lanthoor.spendly.ui.screens.analytics.components.canvas.CustomPieChart

/**
 * Category breakdown pie chart with custom Canvas implementation.
 * Shows top categories by spending with interactive donut chart.
 * Tap on a slice to see category details (icon, name, amount, transaction count) in center.
 * Displays up to 10 categories for better readability.
 */
@Composable
fun CategoryPieChart(
    data: List<PieChartEntry>,
    modifier: Modifier = Modifier
) {
    // Limit to top 10 categories
    val displayData = data.take(10)
    val hasMore = data.size > 10

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.title_spending_by_category),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (hasMore) {
                    Text(
                        text = stringResource(R.string.label_top_n, 10),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp)
            )

            // Empty state
            if (data.isEmpty()) {
                Text(
                    text = stringResource(R.string.label_no_expense_data),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                // Custom Canvas pie chart with tap interaction
                CustomPieChart(data = displayData)
            }
        }
    }
}
