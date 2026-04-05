package dev.lanthoor.spendly.ui.screens.budgets.components

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
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.lanthoor.spendly.R
import dev.lanthoor.spendly.core.model.finance.BudgetWithProgress
import dev.lanthoor.spendly.ui.components.IconMapper.getIcon
import dev.lanthoor.spendly.ui.theme.adjustForTheme
import dev.lanthoor.spendly.ui.theme.budgetProgressColor
import dev.lanthoor.spendly.ui.theme.isDark
import dev.lanthoor.spendly.utils.CurrencyUtils

/**
 * Card component displaying budget progress with color-coded indicator.
 *
 * Color coding:
 * - Green: progress < 75%
 * - Orange: 75% <= progress < 100%
 * - Red: progress >= 100%
 *
 * @param budgetWithProgress Budget with calculated progress
 * @param onClick Click handler for the card
 * @param modifier Modifier for the card
 */
@Composable
fun BudgetProgressCard(
    budgetWithProgress: BudgetWithProgress,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val budget = budgetWithProgress.budget
    val category = budgetWithProgress.category
    val progress = budgetWithProgress.progress.coerceIn(0f, 100f) / 100f // Normalize to 0-1
    val progressPercent = budgetWithProgress.progress

    // Determine progress color
    val progressColor = MaterialTheme.colorScheme.budgetProgressColor(progressPercent / 100f)

    // Calculate remaining/over amount
    val remaining = budget.amount - budgetWithProgress.currentSpent
    val isOverBudget = remaining < 0

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Icon + Name + Progress %
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category icon or overall budget indicator
                    val isDark = MaterialTheme.colorScheme.isDark
                    if (category != null) {
                        Icon(
                            imageVector = getIcon(category.icon),
                            contentDescription = category.name,
                            tint = Color(category.color).adjustForTheme(isDark),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    } else {
                        Icon(
                            imageVector = getIcon("wallet"),
                            contentDescription = stringResource(R.string.label_overall_budget),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.label_overall_budget),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Progress percentage badge
                Text(
                    text = "${progressPercent.toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = progressColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Spent vs Budget amounts
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.label_spent),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = CurrencyUtils.formatPaise(budgetWithProgress.currentSpent),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.label_budget),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = CurrencyUtils.formatPaise(budget.amount),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Remaining/Over budget text
            Text(
                text = if (isOverBudget) {
                    stringResource(
                        R.string.label_over_budget,
                        CurrencyUtils.formatPaise(-remaining)
                    )
                } else {
                    stringResource(R.string.label_remaining, CurrencyUtils.formatPaise(remaining))
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.budgetProgressColor(if (isOverBudget) 1.5f else 0.5f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}
