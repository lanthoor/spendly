package `in`.co.spendly.ui.screens.recurring.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.co.spendly.R
import `in`.co.spendly.domain.model.Account
import `in`.co.spendly.domain.model.Category
import `in`.co.spendly.domain.model.RecurringTransaction
import `in`.co.spendly.ui.components.IconMapper
import `in`.co.spendly.ui.theme.expenseColor
import `in`.co.spendly.ui.theme.incomeColor
import `in`.co.spendly.utils.CurrencyUtils
import `in`.co.spendly.utils.RecurringFrequency
import `in`.co.spendly.utils.TransactionType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * List item for displaying a recurring transaction.
 *
 * @param recurringTransaction The recurring transaction to display
 * @param category The category for the transaction (null if not set)
 * @param account The account for the transaction
 * @param onClick Callback when the item is clicked
 * @param modifier Modifier for the component
 */
@Composable
fun RecurringTransactionListItem(
    recurringTransaction: RecurringTransaction,
    category: Category?,
    account: Account?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transactionType = TransactionType.fromStringOrDefault(recurringTransaction.transactionType)
    val frequency = RecurringFrequency.fromStringOrDefault(recurringTransaction.frequency)

    val amountColor = when (transactionType) {
        TransactionType.EXPENSE -> MaterialTheme.colorScheme.expenseColor()
        TransactionType.INCOME -> MaterialTheme.colorScheme.incomeColor()
    }

    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side: Category icon + details
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category icon
                Icon(
                    imageVector = IconMapper.getIcon(category?.icon ?: "circle"),
                    contentDescription = category?.name,
                    modifier = Modifier.size(40.dp),
                    tint = category?.color?.let { Color(it) } ?: MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Transaction details
                Column {
                    // Description
                    Text(
                        text = recurringTransaction.description,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Category + Account + Next date
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = category?.name ?: stringResource(R.string.label_others),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (account != null) {
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = account.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Frequency chip + Next occurrence
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        SuggestionChip(
                            onClick = {},
                            label = {
                                Text(
                                    text = when (frequency) {
                                        RecurringFrequency.DAILY -> stringResource(R.string.freq_daily)
                                        RecurringFrequency.WEEKLY -> stringResource(R.string.freq_weekly)
                                        RecurringFrequency.MONTHLY -> stringResource(R.string.freq_monthly)
                                    },
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        )

                        Text(
                            text = stringResource(
                                R.string.label_next_occurrence_with_date,
                                dateFormatter.format(Date(recurringTransaction.nextDate))
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Right side: Amount
            Text(
                text = CurrencyUtils.paiseToRupeeString(recurringTransaction.amount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = amountColor
            )
        }
    }
}
