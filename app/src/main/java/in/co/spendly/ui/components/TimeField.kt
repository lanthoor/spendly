package `in`.co.spendly.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Clock
import `in`.co.spendly.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Text field with time picker dialog for selecting time.
 * Displays formatted time and opens Material 3 TimePicker on click.
 *
 * @param selectedTime Currently selected date and time as timestamp (milliseconds)
 * @param onTimeSelected Callback when a time is selected (returns updated timestamp)
 * @param label Label for the text field
 * @param modifier Optional modifier
 * @param enabled Whether the field is enabled
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeField(
    selectedTime: Long,
    onTimeSelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    enabled: Boolean = true
) {
    val displayLabel = label ?: stringResource(R.string.label_time)
    var showDialog by remember { mutableStateOf(false) }
    val timeFormatter = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val formattedTime = remember(selectedTime) {
        timeFormatter.format(Date(selectedTime))
    }

    OutlinedTextField(
        value = formattedTime,
        onValueChange = { /* Read-only */ },
        label = { Text(displayLabel) },
        trailingIcon = {
            Icon(
                imageVector = PhosphorIcons.Regular.Clock,
                contentDescription = stringResource(R.string.cd_select_time),
                modifier = Modifier.clickable(enabled = enabled) { showDialog = true }
            )
        },
        readOnly = true,
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { showDialog = true },
        enabled = enabled,
        singleLine = true
    )

    // Time picker dialog
    if (showDialog) {
        // Extract current time
        val calendar = Calendar.getInstance().apply {
            timeInMillis = selectedTime
        }

        val timePickerState = rememberTimePickerState(
            initialHour = calendar.get(Calendar.HOUR_OF_DAY),
            initialMinute = calendar.get(Calendar.MINUTE),
            is24Hour = false
        )

        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Combine selected time with existing date
                        val updatedCalendar = Calendar.getInstance().apply {
                            timeInMillis = selectedTime  // Preserve date portion
                            set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                            set(Calendar.MINUTE, timePickerState.minute)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        onTimeSelected(updatedCalendar.timeInMillis)
                        showDialog = false
                    }
                ) {
                    Text(stringResource(R.string.button_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(R.string.button_cancel))
                }
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    TimePicker(
                        state = timePickerState
                    )
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TimeFieldPreview() {
    TimeField(
        selectedTime = System.currentTimeMillis(),
        onTimeSelected = {},
        modifier = Modifier.padding(16.dp)
    )
}
