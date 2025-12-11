package `in`.mylullaby.spendly.ui.screens.accounts.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Warning
import `in`.mylullaby.spendly.domain.model.Account
import `in`.mylullaby.spendly.ui.components.AccountDropdown
import `in`.mylullaby.spendly.ui.screens.accounts.TransactionCount

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
        title = { Text("Delete Account?") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "You are about to delete \"${accountToDelete.name}\".",
                    style = MaterialTheme.typography.bodyMedium
                )

                if (transactionCount.totalCount > 0) {
                    Text(
                        text = "This account has ${transactionCount.totalCount} transaction${if (transactionCount.totalCount != 1) "s" else ""} " +
                                "(${transactionCount.expenseCount} expense${if (transactionCount.expenseCount != 1) "s" else ""}, " +
                                "${transactionCount.incomeCount} income).",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )

                    Text(
                        text = "All transactions will be reassigned to the selected account:",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    AccountDropdown(
                        selectedAccount = selectedReplacementAccount,
                        accounts = availableAccounts.filter { it.id != accountToDelete.id },
                        onAccountSelected = { selectedReplacementAccount = it },
                        label = "Reassign to",
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        text = "This account has no transactions and can be safely deleted.",
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
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
