package `in`.mylullaby.spendly.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview

/**
 * Text field for currency amount input in rupees.
 * Accepts decimal input (e.g., "100.50") and shows ₹ prefix.
 * Internally converts to paise using CurrencyUtils.
 *
 * @param value Current amount value as string (in rupees format)
 * @param onValueChange Callback when value changes
 * @param label Label for the text field
 * @param modifier Optional modifier
 * @param isError Whether to show error state
 * @param errorMessage Error message to display
 * @param enabled Whether the field is enabled
 */
@Composable
fun AmountTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "Amount",
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            // Allow only numbers and one decimal point
            if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                onValueChange(newValue)
            }
        },
        label = { Text(label) },
        prefix = { Text("₹ ") },
        modifier = modifier,
        isError = isError,
        supportingText = if (isError && errorMessage != null) {
            { Text(errorMessage) }
        } else null,
        enabled = enabled,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun AmountTextFieldPreview() {
    AmountTextField(
        value = "100.50",
        onValueChange = {},
        label = "Amount"
    )
}

@Preview(showBackground = true)
@Composable
private fun AmountTextFieldErrorPreview() {
    AmountTextField(
        value = "",
        onValueChange = {},
        label = "Amount",
        isError = true,
        errorMessage = "Amount is required"
    )
}
