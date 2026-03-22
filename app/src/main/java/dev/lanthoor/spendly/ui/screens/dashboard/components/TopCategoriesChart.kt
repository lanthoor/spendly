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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.CaretDown
import com.adamglin.phosphoricons.regular.CaretUp
import dev.lanthoor.spendly.R
import dev.lanthoor.spendly.ui.components.IconMapper
import dev.lanthoor.spendly.ui.screens.dashboard.CategorySpending
import dev.lanthoor.spendly.utils.CurrencyUtils

/**
 * Chart displaying spending categories as horizontal bars with expand/collapse
 */
@Composable
fun TopCategoriesChart(
    categories: List<CategorySpending>,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

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
                text = stringResource(R.string.label_spend_categories),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

            if (categories.isEmpty()) {
                Text(
                    text = stringResource(R.string.msg_no_expense_data_yet),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                val maxAmount = categories.maxOfOrNull { it.totalAmount } ?: 1L
                val displayCategories = if (isExpanded) categories else categories.take(3)

                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    displayCategories.forEach { categorySpending ->
                        CategoryBar(
                            categorySpending = categorySpending,
                            maxAmount = maxAmount
                        )
                    }
                }

                // Show All / Show Less button
                if (categories.size > 3) {
                    TextButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isExpanded) stringResource(R.string.action_show_less) else stringResource(
                                R.string.action_show_all
                            ),
                            style = MaterialTheme.typography.labelLarge
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = if (isExpanded)
                                PhosphorIcons.Regular.CaretUp
                            else PhosphorIcons.Regular.CaretDown,
                            contentDescription = null
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
                        text = pluralStringResource(
                            R.plurals.plurals_transactions,
                            categorySpending.transactionCount,
                            categorySpending.transactionCount
                        ),
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
