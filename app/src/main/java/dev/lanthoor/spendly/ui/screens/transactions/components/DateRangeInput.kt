package dev.lanthoor.spendly.ui.screens.transactions.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.CalendarBlank
import com.adamglin.phosphoricons.regular.CaretDown
import dev.lanthoor.spendly.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Date range filter dropdown with popup dialog for manual input and calendar selection.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangeFilterChip(
    startDate: Long?,
    endDate: Long?,
    onDateRangeChange: (Long?, Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val dateFormatter = SimpleDateFormat("dd-MM-yy", Locale.getDefault())
    val dateInputFormatter = SimpleDateFormat("dd/MM/yy", Locale.getDefault())

    // Button label
    val buttonLabel = when {
        startDate != null && endDate != null -> {
            "${dateFormatter.format(Date(startDate))} - ${dateFormatter.format(Date(endDate))}"
        }

        else -> stringResource(R.string.label_all)
    }

    val dateRangePickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = startDate,
        initialSelectedEndDateMillis = endDate
    )

    // Text field states
    var startDateText by remember(startDate) {
        mutableStateOf(startDate?.let { dateInputFormatter.format(Date(it)) } ?: "")
    }
    var endDateText by remember(endDate) {
        mutableStateOf(endDate?.let { dateInputFormatter.format(Date(it)) } ?: "")
    }

    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { expanded = true }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = PhosphorIcons.Regular.CalendarBlank,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = buttonLabel,
                    modifier = Modifier.padding(start = 4.dp),
                    style = MaterialTheme.typography.bodySmall
                )
                Icon(
                    imageVector = PhosphorIcons.Regular.CaretDown,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // DropdownMenu with manual input and calendar picker
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            Column(
                modifier = Modifier
                    .width(500.dp)
                    .height(550.dp)
                    .padding(16.dp)
            ) {
                // Manual input section
                Text(
                    text = stringResource(R.string.label_enter_dates_manually),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Start date input
                    OutlinedTextField(
                        value = startDateText,
                        onValueChange = { newValue ->
                            startDateText = newValue
                            tryParseDate(newValue)?.let { parsedDate ->
                                dateRangePickerState.setSelection(
                                    parsedDate,
                                    dateRangePickerState.selectedEndDateMillis
                                )
                            }
                        },
                        label = {
                            Text(
                                stringResource(R.string.label_date),
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        placeholder = {
                            Text(
                                stringResource(R.string.placeholder_date_format),
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        textStyle = MaterialTheme.typography.bodySmall,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    // End date input
                    OutlinedTextField(
                        value = endDateText,
                        onValueChange = { newValue ->
                            endDateText = newValue
                            tryParseDate(newValue)?.let { parsedDate ->
                                dateRangePickerState.setSelection(
                                    dateRangePickerState.selectedStartDateMillis,
                                    parsedDate
                                )
                            }
                        },
                        label = {
                            Text(
                                stringResource(R.string.label_date),
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        placeholder = {
                            Text(
                                stringResource(R.string.placeholder_date_format),
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        textStyle = MaterialTheme.typography.bodySmall,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                // Calendar picker section
                Text(
                    text = stringResource(R.string.label_or_select_from_calendar),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                ) {
                    DateRangePicker(
                        state = dateRangePickerState,
                        title = null,
                        headline = null,
                        showModeToggle = false
                    )
                }

                // Action buttons
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (dateRangePickerState.selectedStartDateMillis != null ||
                        dateRangePickerState.selectedEndDateMillis != null
                    ) {
                        TextButton(
                            onClick = {
                                onDateRangeChange(null, null)
                                expanded = false
                            }
                        ) {
                            Text(stringResource(R.string.button_clear))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    TextButton(onClick = { expanded = false }) {
                        Text(stringResource(R.string.button_cancel))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = {
                            // Update text fields from picker state
                            startDateText = dateRangePickerState.selectedStartDateMillis?.let {
                                dateInputFormatter.format(Date(it))
                            } ?: ""
                            endDateText = dateRangePickerState.selectedEndDateMillis?.let {
                                dateInputFormatter.format(Date(it))
                            } ?: ""

                            onDateRangeChange(
                                dateRangePickerState.selectedStartDateMillis,
                                dateRangePickerState.selectedEndDateMillis
                            )
                            expanded = false
                        }
                    ) {
                        Text(stringResource(R.string.button_apply))
                    }
                }
            }
        }
    }
}

/**
 * Try to parse date string in dd/MM/yy format.
 * Returns timestamp in milliseconds or null if invalid.
 */
private fun tryParseDate(dateString: String): Long? {
    if (dateString.length < 6) return null

    return try {
        val formatter = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
        formatter.isLenient = false
        formatter.parse(dateString)?.time
    } catch (e: Exception) {
        // Try with full year format
        try {
            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            formatter.isLenient = false
            formatter.parse(dateString)?.time
        } catch (e2: Exception) {
            null
        }
    }
}
