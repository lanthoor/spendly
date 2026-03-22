package dev.lanthoor.spendly.ui.screens.analytics.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.lanthoor.spendly.ui.screens.analytics.AnalyticsPeriodType

/**
 * Segmented button for analytics period selection (This Month / Financial Year / Calendar Year).
 * Displayed in the top app bar, centered horizontally.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsPeriodSegmentedButton(
    selectedPeriod: AnalyticsPeriodType,
    onPeriodSelected: (AnalyticsPeriodType) -> Unit,
    modifier: Modifier = Modifier
) {
    SingleChoiceSegmentedButtonRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(end = 16.dp)
    ) {
        AnalyticsPeriodType.entries.forEachIndexed { index, period ->
            SegmentedButton(
                selected = selectedPeriod == period,
                onClick = { onPeriodSelected(period) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = AnalyticsPeriodType.entries.size
                ),
                icon = {
                    // Empty icon slot to remove the default check icon
                },
                label = {
                    Text(
                        text = period.getDisplayName(),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (selectedPeriod == period) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )
        }
    }
}
