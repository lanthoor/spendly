package `in`.co.spendly.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.CaretDown
import `in`.co.spendly.R
import `in`.co.spendly.domain.model.Account
import `in`.co.spendly.utils.AccountType

/**
 * Dropdown menu for selecting an account.
 * Displays account icon and name for each option in a scrollable list.
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
    modifier: Modifier = Modifier,
    label: String? = null,
    enabled: Boolean = true
) {
    val displayLabel = label ?: stringResource(R.string.label_account)
    var expanded by remember { mutableStateOf(false) }
    var buttonWidth by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current

    Box(modifier = modifier) {
        Column(
            modifier = Modifier.onSizeChanged { buttonWidth = it.width }
        ) {
            // Label text
            Text(
                text = displayLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
            )

            // Button trigger
            OutlinedButton(
                onClick = { if (enabled) expanded = true },
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                contentPadding = PaddingValues(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Leading icon
                    selectedAccount?.let { account ->
                        Icon(
                            imageVector = IconMapper.getIcon(account.icon),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = Color(account.color)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    // Text
                    Text(
                        text = selectedAccount?.name ?: stringResource(R.string.label_my_account),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Start
                    )

                    // Trailing caret
                    Icon(
                        imageVector = PhosphorIcons.Regular.CaretDown,
                        contentDescription = stringResource(R.string.cd_select_account),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Dropdown menu
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = with(density) { Modifier.width(buttonWidth.toDp()) }
        ) {
            Column(
                modifier = Modifier
                    .heightIn(max = 360.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                accounts.forEach { account ->
                    DropdownMenuItem(
                        text = { Text(account.name) },
                        onClick = {
                            onAccountSelected(account)
                            expanded = false
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = IconMapper.getIcon(account.icon),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = Color(account.color)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (selectedAccount?.id == account.id) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    Color.Transparent
                                }
                            )
                    )
                }
            }
        }
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
