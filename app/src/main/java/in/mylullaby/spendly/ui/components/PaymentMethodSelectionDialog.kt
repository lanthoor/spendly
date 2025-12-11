package `in`.mylullaby.spendly.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Bank
import com.adamglin.phosphoricons.regular.CreditCard
import com.adamglin.phosphoricons.regular.Money
import com.adamglin.phosphoricons.regular.QrCode
import com.adamglin.phosphoricons.regular.Wallet
import `in`.mylullaby.spendly.utils.PaymentMethod

/**
 * Dialog for selecting a payment method with grid layout
 *
 * @param selectedMethod Currently selected payment method
 * @param onMethodSelected Callback when a payment method is selected
 * @param onDismiss Callback when dialog is dismissed
 */
@Composable
fun PaymentMethodSelectionDialog(
    selectedMethod: PaymentMethod,
    onMethodSelected: (PaymentMethod) -> Unit,
    onDismiss: () -> Unit
) {
    var tempSelection by remember { mutableStateOf(selectedMethod) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Payment Method") },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(PaymentMethod.entries) { method ->
                    PaymentMethodGridItem(
                        method = method,
                        isSelected = tempSelection == method,
                        onClick = { tempSelection = method }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onMethodSelected(tempSelection)
                    onDismiss()
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun PaymentMethodGridItem(
    method: PaymentMethod,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        Color.Transparent
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = method.getIcon(),
            contentDescription = method.toDisplayName(),
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = method.toDisplayName(),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
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
