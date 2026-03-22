package dev.lanthoor.spendly.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.lanthoor.spendly.R
import dev.lanthoor.spendly.utils.TimePeriod

/**
 * Dialog for selecting analytics time period.
 * Provides quick selections (This Month, Last Month, etc.) and custom date range option.
 */
@Composable
fun TimePeriodSelectionDialog(
    selectedPeriod: TimePeriod,
    onPeriodSelected: (TimePeriod) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var tempSelection by remember { mutableStateOf(selectedPeriod) }
    var showCustomRangePicker by remember { mutableStateOf(false) }

    // If custom range picker is shown, display it instead
    if (showCustomRangePicker) {
        DateRangePickerModal(
            onDateRangeSelected = { startDate, endDate ->
                if (startDate != null && endDate != null) {
                    tempSelection = TimePeriod.Custom(startDate, endDate)
                }
                showCustomRangePicker = false
            },
            onDismiss = {
                showCustomRangePicker = false
            }
        )
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.title_select_time_period)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Quick selection chips (2 columns x 3 rows)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Define quick selections
                    val quickSelections: List<Pair<TimePeriod, Int>> = listOf(
                        TimePeriod.ThisMonth to R.string.tp_this_month,
                        TimePeriod.LastMonth to R.string.tp_last_month,
                        TimePeriod.Last3Months to R.string.tp_last_3_months,
                        TimePeriod.Last6Months to R.string.tp_last_6_months,
                        TimePeriod.ThisYear to R.string.tp_this_year,
                        TimePeriod.LastYear to R.string.tp_last_year
                    )

                    items(quickSelections.size) { index ->
                        val pair = quickSelections[index]
                        val period = pair.first
                        val labelRes = pair.second
                        FilterChip(
                            selected = tempSelection::class == period::class,
                            onClick = { tempSelection = period },
                            label = { Text(stringResource(labelRes)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Custom date range button
                OutlinedButton(
                    onClick = { showCustomRangePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.tp_custom_date_range))
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onPeriodSelected(tempSelection)
                    onDismiss()
                }
            ) {
                Text(stringResource(R.string.button_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.button_cancel))
            }
        },
        modifier = modifier
    )
}
