package dev.lanthoor.spendly.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.CaretLeft
import com.adamglin.phosphoricons.regular.CaretRight
import dev.lanthoor.spendly.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Dialog for selecting a month and year.
 */
@Composable
fun MonthPickerDialog(
    selectedYear: Int,
    selectedMonth: Int,
    onMonthSelected: (year: Int, month: Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var tempYear by remember { mutableIntStateOf(selectedYear) }
    var tempMonth by remember { mutableIntStateOf(selectedMonth) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.title_select_month)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Year selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { tempYear-- }) {
                        Icon(
                            imageVector = PhosphorIcons.Regular.CaretLeft,
                            contentDescription = stringResource(R.string.cd_previous_year)
                        )
                    }
                    Text(
                        text = tempYear.toString(),
                        style = MaterialTheme.typography.titleLarge
                    )
                    IconButton(onClick = { tempYear++ }) {
                        Icon(
                            imageVector = PhosphorIcons.Regular.CaretRight,
                            contentDescription = stringResource(R.string.cd_next_year)
                        )
                    }
                }

                // Month grid (4 columns x 3 rows)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(
                        8.dp,
                        Alignment.CenterHorizontally
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(12) { monthIndex ->
                        val month = monthIndex + 1
                        FilterChip(
                            selected = month == tempMonth && tempYear == selectedYear,
                            onClick = { tempMonth = month },
                            modifier = Modifier.fillMaxWidth(),
                            label = {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(getMonthShortName(month))
                                }
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onMonthSelected(tempYear, tempMonth)
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

/**
 * Get short month name (e.g., "Jan", "Feb", "Mar").
 * Month is 1-indexed (1=Jan, 12=Dec).
 */
private fun getMonthShortName(month: Int): String {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.MONTH, month - 1)
    }
    return SimpleDateFormat("MMM", Locale.getDefault()).format(Date(calendar.timeInMillis))
}
