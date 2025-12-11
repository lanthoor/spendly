package `in`.mylullaby.spendly.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.CalendarBlank
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Text field with date picker dialog for selecting dates.
 * Displays formatted date and opens Material 3 DatePicker on click.
 *
 * @param selectedDate Currently selected date as timestamp (milliseconds)
 * @param onDateSelected Callback when a date is selected
 * @param label Label for the text field
 * @param modifier Optional modifier
 * @param enabled Whether the field is enabled
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    selectedDate: Long,
    onDateSelected: (Long) -> Unit,
    label: String = "Date",
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var showDialog by remember { mutableStateOf(false) }
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val formattedDate = remember(selectedDate) {
        dateFormatter.format(Date(selectedDate))
    }

    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = formattedDate,
            onValueChange = { /* Read-only */ },
            label = { Text(label) },
            trailingIcon = {
                Icon(
                    imageVector = PhosphorIcons.Regular.CalendarBlank,
                    contentDescription = "Select date",
                    modifier = Modifier.clickable(enabled = enabled) { showDialog = true }
                )
            },
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled) { showDialog = true },
            enabled = enabled,
            singleLine = true
        )
    }

    // Date picker dialog
    if (showDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate
        )
        val confirmEnabled by remember {
            derivedStateOf { datePickerState.selectedDateMillis != null }
        }

        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            onDateSelected(millis)
                        }
                        showDialog = false
                    },
                    enabled = confirmEnabled
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DatePickerFieldPreview() {
    DatePickerField(
        selectedDate = System.currentTimeMillis(),
        onDateSelected = {},
        modifier = Modifier.padding(16.dp)
    )
}
