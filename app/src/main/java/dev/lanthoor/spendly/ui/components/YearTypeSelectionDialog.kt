package dev.lanthoor.spendly.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.lanthoor.spendly.R
import dev.lanthoor.spendly.core.model.preferences.YearType
import dev.lanthoor.spendly.core.ui.format.getDisplayRange
import dev.lanthoor.spendly.core.ui.format.toDisplayName

/**
 * Dialog for selecting financial year type.
 */
@Composable
fun YearTypeSelectionDialog(
    currentYearType: YearType,
    onYearTypeSelected: (YearType) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.title_financial_year_type)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                YearType.entries.forEach { type ->
                    Card(
                        onClick = {
                            onYearTypeSelected(type)
                            onDismiss()
                        },
                        colors = CardDefaults.cardColors(
                            containerColor = if (type == currentYearType) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = type.toDisplayName(),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = type.getDisplayRange(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.button_close))
            }
        },
        modifier = modifier
    )
}
