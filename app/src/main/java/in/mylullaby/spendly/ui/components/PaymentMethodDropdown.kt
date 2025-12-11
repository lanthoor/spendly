package `in`.mylullaby.spendly.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Bank
import com.adamglin.phosphoricons.regular.CaretDown
import com.adamglin.phosphoricons.regular.CreditCard
import com.adamglin.phosphoricons.regular.Money
import com.adamglin.phosphoricons.regular.QrCode
import com.adamglin.phosphoricons.regular.Wallet
import `in`.mylullaby.spendly.utils.PaymentMethod
import kotlinx.coroutines.flow.collectLatest

/**
 * Dropdown menu for selecting a payment method.
 * Displays friendly names for each payment method enum value.
 *
 * @param selectedMethod Currently selected payment method
 * @param onMethodSelected Callback when a payment method is selected
 * @param label Label for the dropdown field
 * @param modifier Optional modifier
 * @param enabled Whether the field is enabled
 */
@Composable
fun PaymentMethodDropdown(
    selectedMethod: PaymentMethod,
    onMethodSelected: (PaymentMethod) -> Unit,
    label: String = "Payment Method",
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var showDialog by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collectLatest { interaction ->
            if (interaction is PressInteraction.Release) {
                showDialog = true
            }
        }
    }

    OutlinedTextField(
        value = selectedMethod.toDisplayName(),
        onValueChange = { /* Read-only */ },
        label = { Text(label) },
        leadingIcon = {
            Icon(
                imageVector = selectedMethod.getIcon(),
                contentDescription = selectedMethod.toDisplayName()
            )
        },
        trailingIcon = {
            Icon(
                imageVector = PhosphorIcons.Regular.CaretDown,
                contentDescription = "Select payment method"
            )
        },
        readOnly = true,
        interactionSource = interactionSource,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        singleLine = true
    )

    if (showDialog) {
        PaymentMethodSelectionDialog(
            selectedMethod = selectedMethod,
            onMethodSelected = onMethodSelected,
            onDismiss = { showDialog = false }
        )
    }
}

/**
 * Get icon for payment method
 */
private fun PaymentMethod.getIcon(): ImageVector {
    return when (this) {
        PaymentMethod.CASH -> PhosphorIcons.Regular.Money
        PaymentMethod.UPI -> PhosphorIcons.Regular.QrCode
        PaymentMethod.DEBIT_CARD -> PhosphorIcons.Regular.CreditCard
        PaymentMethod.CREDIT_CARD -> PhosphorIcons.Regular.CreditCard
        PaymentMethod.NET_BANKING -> PhosphorIcons.Regular.Bank
        PaymentMethod.WALLET -> PhosphorIcons.Regular.Wallet
    }
}

/**
 * Convert payment method to display name
 */
private fun PaymentMethod.toDisplayName(): String {
    return when (this) {
        PaymentMethod.CASH -> "Cash"
        PaymentMethod.UPI -> "UPI"
        PaymentMethod.DEBIT_CARD -> "Debit Card"
        PaymentMethod.CREDIT_CARD -> "Credit Card"
        PaymentMethod.NET_BANKING -> "Net Banking"
        PaymentMethod.WALLET -> "Wallet"
    }
}

@Preview(showBackground = true)
@Composable
private fun PaymentMethodDropdownPreview() {
    PaymentMethodDropdown(
        selectedMethod = PaymentMethod.UPI,
        onMethodSelected = {},
        modifier = Modifier.padding(16.dp)
    )
}
