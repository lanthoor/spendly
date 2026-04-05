package dev.lanthoor.spendly.ui.screens.analytics.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.lanthoor.spendly.core.model.preferences.TimePeriod
import dev.lanthoor.spendly.core.ui.format.toDisplayName

/**
 * Chip component displaying the current time period.
 * Tapping opens a selection dialog (to be implemented).
 */
@Composable
fun TimePeriodChip(
    period: TimePeriod,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = true,
        onClick = onClick,
        label = {
            Text(
                text = period.toDisplayName(),
                style = MaterialTheme.typography.labelLarge
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        modifier = modifier.padding(8.dp)
    )
}
