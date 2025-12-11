package `in`.mylullaby.spendly.ui.screens.income.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import `in`.mylullaby.spendly.domain.model.Income
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * List item displaying income information with green accent.
 * Shows source icon, description, date/source, and amount.
 *
 * @param income Income to display
 * @param onClick Callback when item is clicked
 * @param modifier Optional modifier
 */
@Composable
fun IncomeListItem(
    income: Income,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val formattedDate = dateFormatter.format(Date(income.date))

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Income source icon
            Icon(
                imageVector = income.source.getIcon(),
                contentDescription = income.source.toDisplayString(),
                tint = Color(0xFF2E7D32), // Green for income
                modifier = Modifier.padding(end = 16.dp)
            )

            // Description and details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = income.description,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = " • ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = income.source.toDisplayString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Amount in green
            Text(
                text = income.displayAmount(),
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF2E7D32) // Green for income amount
            )
        }

        HorizontalDivider()
    }
}
