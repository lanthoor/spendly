package dev.lanthoor.spendly.ui.screens.accounts.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Warning
import dev.lanthoor.spendly.R
import dev.lanthoor.spendly.domain.model.Account
import dev.lanthoor.spendly.ui.components.AccountDropdown
import dev.lanthoor.spendly.ui.screens.accounts.TransactionCount

/**
 * Dialog for confirming account deletion with transaction reassignment.
 */
@Composable
fun DeleteAccountDialog(
    accountToDelete: Account,
    transactionCount: TransactionCount,
    availableAccounts: List<Account>,
    onConfirm: (replacementAccountId: Long) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedReplacementAccount by remember {
        mutableStateOf(
            availableAccounts.firstOrNull { it.id != accountToDelete.id }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = PhosphorIcons.Regular.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = { Text(stringResource(R.string.title_delete_account)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(
                        R.string.msg_delete_account_confirm,
                        accountToDelete.name
                    ),
                    style = MaterialTheme.typography.bodyMedium
                )

                if (transactionCount.totalCount > 0) {
                    Text(
                        text = pluralStringResource(
                            R.plurals.plurals_account_transactions,
                            transactionCount.totalCount,
                            transactionCount.totalCount,
                            transactionCount.expenseCount,
                            transactionCount.incomeCount
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )

                    Text(
                        text = stringResource(R.string.msg_transactions_reassign),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    AccountDropdown(
                        selectedAccount = selectedReplacementAccount,
                        accounts = availableAccounts.filter { it.id != accountToDelete.id },
                        onAccountSelected = { selectedReplacementAccount = it },
                        label = stringResource(R.string.label_reassign_to),
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        text = stringResource(R.string.msg_account_no_transactions),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedReplacementAccount?.let { replacement ->
                        onConfirm(replacement.id)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                enabled = transactionCount.totalCount == 0 || selectedReplacementAccount != null
            ) {
                Text(stringResource(R.string.button_delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.button_cancel))
            }
        }
    )
}
