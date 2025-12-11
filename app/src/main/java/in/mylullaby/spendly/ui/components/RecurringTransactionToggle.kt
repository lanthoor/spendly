package `in`.mylullaby.spendly.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Recurring transaction toggle component.
 * Shows a switch to enable recurring transactions, and when enabled,
 * displays frequency dropdown and next occurrence date.
 *
 * @param isRecurring Whether the transaction is recurring
 * @param frequency The frequency of the recurring transaction (DAILY, WEEKLY, MONTHLY)
 * @param nextDate The next occurrence date in milliseconds
 * @param onRecurringChange Callback when recurring toggle changes
 * @param onFrequencyChange Callback when frequency changes
 * @param onNextDateChange Callback when next date changes
 * @param modifier Optional modifier
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringTransactionToggle(
    isRecurring: Boolean,
    frequency: String,
    nextDate: Long,
    onRecurringChange: (Boolean) -> Unit,
    onFrequencyChange: (String) -> Unit,
    onNextDateChange: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Recurring toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recurring Transaction",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = isRecurring,
                onCheckedChange = onRecurringChange
            )
        }

        // Show frequency dropdown when recurring is enabled
        if (isRecurring) {
            Spacer(modifier = Modifier.height(16.dp))

            // Frequency dropdown
            var expanded by remember { mutableStateOf(false) }
            val frequencies = listOf("DAILY", "WEEKLY", "MONTHLY")

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = frequency.lowercase().replaceFirstChar { it.uppercase() },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Frequency") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    frequencies.forEach { freq ->
                        DropdownMenuItem(
                            text = { Text(freq.lowercase().replaceFirstChar { it.uppercase() }) },
                            onClick = {
                                onFrequencyChange(freq)
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Next occurrence date picker
            DatePickerField(
                label = "Next Occurrence",
                selectedDate = nextDate,
                onDateSelected = onNextDateChange,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "This transaction will be automatically created on the scheduled date",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}
