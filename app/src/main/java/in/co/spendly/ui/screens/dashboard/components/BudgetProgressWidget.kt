package `in`.co.spendly.ui.screens.dashboard.components

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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import `in`.co.spendly.R
import `in`.co.spendly.ui.screens.budgets.BudgetWithProgress
import `in`.co.spendly.ui.theme.budgetProgressColor
import `in`.co.spendly.utils.CurrencyUtils

/**
 * Dashboard widget showing top 3 budgets by progress percentage.
 * Displays mini progress bars with category names.
 *
 * @param budgets List of budgets with progress (max 3 shown)
 * @param onViewAllClick Callback when "View All" button is clicked (unused, kept for compatibility)
 * @param modifier Modifier for the card
 */
@Composable
fun BudgetProgressWidget(
    budgets: List<BudgetWithProgress>,
    onViewAllClick: () -> Unit,
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
                text = stringResource(R.string.label_budget_overview),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

            // Show top 3 budgets or empty state
            if (budgets.isEmpty()) {
                Text(
                    text = stringResource(R.string.label_no_budgets_configured),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                budgets.take(3).forEach { budgetWithProgress ->
                    BudgetMiniItem(budgetWithProgress = budgetWithProgress)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

/**
 * Mini budget item for the dashboard widget.
 * Shows category name, progress bar, and amounts.
 */
@Composable
private fun BudgetMiniItem(
    budgetWithProgress: BudgetWithProgress,
    modifier: Modifier = Modifier
) {
    val budget = budgetWithProgress.budget
    val category = budgetWithProgress.category
    val progress = budgetWithProgress.progress.coerceIn(0f, 100f) / 100f
    val progressPercent = budgetWithProgress.progress

    // Determine progress color based on budget usage
    val progressColor = MaterialTheme.colorScheme.budgetProgressColor(progressPercent / 100f)

    Column(modifier = modifier.fillMaxWidth()) {
        // Category name and progress percentage
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = category?.name ?: stringResource(R.string.label_overall_budget),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = stringResource(R.string.label_percentage, progressPercent.toInt()),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = progressColor
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Progress bar
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = progressColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Spent / Budget amounts
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = CurrencyUtils.formatPaise(budgetWithProgress.currentSpent),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.label_of, CurrencyUtils.formatPaise(budget.amount)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
