package dev.lanthoor.spendly.ui.screens.recurring.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import dev.lanthoor.spendly.R
import dev.lanthoor.spendly.utils.RecurringFrequency

/**
 * Segmented button for selecting recurring frequency (Daily/Weekly/Monthly).
 *
 * @param selectedFrequency Currently selected frequency
 * @param onFrequencySelected Callback when a frequency is selected
 * @param modifier Modifier for the component
 */
@Composable
fun FrequencySelectionButton(
    selectedFrequency: RecurringFrequency,
    onFrequencySelected: (RecurringFrequency) -> Unit,
    modifier: Modifier = Modifier
) {
    val frequencies = RecurringFrequency.entries

    SingleChoiceSegmentedButtonRow(
        modifier = modifier.fillMaxWidth()
    ) {
        frequencies.forEachIndexed { index, frequency ->
            SegmentedButton(
                selected = selectedFrequency == frequency,
                onClick = { onFrequencySelected(frequency) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = frequencies.size
                ),
                label = {
                    Text(
                        text = when (frequency) {
                            RecurringFrequency.DAILY -> stringResource(R.string.freq_daily)
                            RecurringFrequency.WEEKLY -> stringResource(R.string.freq_weekly)
                            RecurringFrequency.MONTHLY -> stringResource(R.string.freq_monthly)
                        },
                        fontWeight = if (selectedFrequency == frequency) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            )
        }
    }
}
