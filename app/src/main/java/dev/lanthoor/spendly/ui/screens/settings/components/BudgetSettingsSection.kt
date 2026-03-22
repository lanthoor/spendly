package dev.lanthoor.spendly.ui.screens.settings.components

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
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Plus
import dev.lanthoor.spendly.R
import dev.lanthoor.spendly.ui.components.IconMapper.getIcon
import dev.lanthoor.spendly.ui.screens.budgets.BudgetWithProgress
import dev.lanthoor.spendly.ui.theme.adjustForTheme
import dev.lanthoor.spendly.ui.theme.budgetProgressColor
import dev.lanthoor.spendly.ui.theme.isDark
import dev.lanthoor.spendly.utils.CurrencyUtils

/**
 * Budget settings section showing current budgets with progress.
 * Displays in Settings screen.
 *
 * @param budgets List of budgets with progress for current month
 * @param onAddBudget Callback to add new budget
 * @param onEditBudget Callback to edit existing budget with ID
 */
@Composable
fun BudgetSettingsSection(
    budgets: List<BudgetWithProgress>,
    onAddBudget: () -> Unit,
    onEditBudget: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Add budget button
        Card(
            onClick = onAddBudget,
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = PhosphorIcons.Regular.Plus,
                    contentDescription = stringResource(R.string.button_add_budget),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.button_add_budget),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (budgets.isEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.label_no_budgets_configured),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        } else {
            Spacer(modifier = Modifier.height(12.dp))

            // Budget cards
            budgets.forEach { budgetWithProgress ->
                BudgetSettingCard(
                    budgetWithProgress = budgetWithProgress,
                    onClick = { onEditBudget(budgetWithProgress.budget.id) },
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
    }
}

/**
 * Individual budget card for settings section.
 */
@Composable
private fun BudgetSettingCard(
    budgetWithProgress: BudgetWithProgress,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val budget = budgetWithProgress.budget
    val category = budgetWithProgress.category
    val progress = budgetWithProgress.progress.coerceIn(0f, 100f) / 100f
    val progressPercent = budgetWithProgress.progress

    // Determine progress color
    val progressColor = MaterialTheme.colorScheme.budgetProgressColor(progressPercent / 100f)

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
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Category icon or overall budget indicator
                    val isDark = MaterialTheme.colorScheme.isDark
                    if (category != null) {
                        Icon(
                            imageVector = getIcon(category.icon),
                            contentDescription = category.name,
                            tint = Color(category.color).adjustForTheme(isDark),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Icon(
                            imageVector = getIcon("wallet"),
                            contentDescription = stringResource(R.string.label_overall_budget),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.label_overall_budget),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Progress percentage
                Text(
                    text = "${progressPercent.toInt()}%",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = progressColor
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.surface,
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Spent / Budget amounts
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = CurrencyUtils.formatPaise(budgetWithProgress.currentSpent),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = stringResource(
                        R.string.label_of,
                        CurrencyUtils.formatPaise(budget.amount)
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
