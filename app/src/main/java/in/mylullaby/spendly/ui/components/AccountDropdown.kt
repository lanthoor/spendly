package `in`.mylullaby.spendly.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.CaretDown
import `in`.mylullaby.spendly.domain.model.Account
import `in`.mylullaby.spendly.utils.AccountType
import kotlinx.coroutines.flow.collectLatest

/**
 * Dropdown menu for selecting an account.
 * Displays account icon, name, and type badge for each option.
 *
 * @param selectedAccount Currently selected account
 * @param accounts List of available accounts
 * @param onAccountSelected Callback when an account is selected
 * @param label Label for the dropdown field
 * @param modifier Optional modifier
 * @param enabled Whether the field is enabled
 */
@Composable
fun AccountDropdown(
    selectedAccount: Account?,
    accounts: List<Account>,
    onAccountSelected: (Account) -> Unit,
    label: String = "Account",
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
        value = selectedAccount?.name ?: "My Account",
        onValueChange = { /* Read-only */ },
        label = { Text(label) },
        leadingIcon = selectedAccount?.let { account ->
            {
                Icon(
                    imageVector = IconMapper.getIcon(account.icon),
                    contentDescription = account.name,
                    tint = Color(account.color)
                )
            }
        },
        trailingIcon = {
            Icon(
                imageVector = PhosphorIcons.Regular.CaretDown,
                contentDescription = "Select account"
            )
        },
        readOnly = true,
        interactionSource = interactionSource,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        singleLine = true
    )

    if (showDialog) {
        AccountSelectionDialog(
            selectedAccount = selectedAccount,
            accounts = accounts,
            onAccountSelected = onAccountSelected,
            onDismiss = { showDialog = false }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AccountDropdownPreview() {
    val sampleAccounts = listOf(
        Account(
            id = 1,
            name = "My Account",
            type = AccountType.BANK,
            icon = "bank",
            color = 0xFF00BFA5.toInt(),
            isCustom = false,
            sortOrder = 1,
            createdAt = 0,
            modifiedAt = 0
        ),
        Account(
            id = 2,
            name = "HDFC Credit Card",
            type = AccountType.CARD,
            icon = "creditcard",
            color = 0xFFE91E63.toInt(),
            isCustom = true,
            sortOrder = 2,
            createdAt = 0,
            modifiedAt = 0
        )
    )

    AccountDropdown(
        selectedAccount = sampleAccounts[0],
        accounts = sampleAccounts,
        onAccountSelected = {},
        modifier = Modifier.padding(16.dp)
    )
}
