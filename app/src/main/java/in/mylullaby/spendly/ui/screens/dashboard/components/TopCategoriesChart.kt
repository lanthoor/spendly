package `in`.mylullaby.spendly.ui.screens.dashboard.components

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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.mylullaby.spendly.ui.components.IconMapper
import `in`.mylullaby.spendly.ui.screens.dashboard.CategorySpending
import `in`.mylullaby.spendly.utils.CurrencyUtils

/**
 * Chart displaying top 3 spending categories as horizontal bars
 */
@Composable
fun TopCategoriesChart(
    categories: List<CategorySpending>,
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Text(
                text = "Top Spending Categories",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            if (categories.isEmpty()) {
                Text(
                    text = "No expense data yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                val maxAmount = categories.maxOfOrNull { it.totalAmount } ?: 1L

                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    categories.forEach { categorySpending ->
                        CategoryBar(
                            categorySpending = categorySpending,
                            maxAmount = maxAmount
                        )
                    }
                }
            }
        }
    }
}

/**
 * Single category bar showing spending
 */
@Composable
private fun CategoryBar(
    categorySpending: CategorySpending,
    maxAmount: Long,
    modifier: Modifier = Modifier
) {
    val progress = if (maxAmount > 0) {
        (categorySpending.totalAmount.toFloat() / maxAmount.toFloat())
    } else {
        0f
    }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Category name and amount
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category icon and name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = IconMapper.getIcon(categorySpending.category.icon),
                    contentDescription = categorySpending.category.name,
                    tint = Color(categorySpending.category.color),
                    modifier = Modifier.padding(end = 8.dp)
                )

                Column {
                    Text(
                        text = categorySpending.category.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${categorySpending.transactionCount} transaction${if (categorySpending.transactionCount != 1) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Amount
            Text(
                text = CurrencyUtils.formatPaise(categorySpending.totalAmount),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Progress bar
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = Color(categorySpending.category.color),
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}
