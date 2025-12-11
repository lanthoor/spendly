package `in`.mylullaby.spendly.ui.screens.accounts

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Trash
import `in`.mylullaby.spendly.domain.model.Account
import `in`.mylullaby.spendly.ui.screens.accounts.components.AccountFormFields
import `in`.mylullaby.spendly.ui.screens.accounts.components.DeleteAccountDialog

/**
 * Modal bottom sheet for editing an existing account.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAccountScreen(
    accountId: Long,
    onDismiss: () -> Unit,
    viewModel: AccountViewModel = hiltViewModel()
) {
    val formState by viewModel.formState.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    var isSaving by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Find account from the list
    val accountToEdit = remember(accounts, accountId) {
        accounts.find { it.id == accountId }
    }

    // Initialize form when account is found
    LaunchedEffect(accountToEdit) {
        accountToEdit?.let { account ->
            viewModel.initializeFormForEdit(account)
        }
    }

    // Don't render anything if account not found
    if (accountToEdit == null) {
        return
    }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            // Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Edit Account",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.weight(1f)
                )

                // Delete button (only if not default account)
                if (accountToEdit.id != Account.DEFAULT_ACCOUNT_ID) {
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = PhosphorIcons.Regular.Trash,
                            contentDescription = "Delete account"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Form Fields
            AccountFormFields(
                formState = formState,
                onFieldChange = { field, value ->
                    viewModel.updateFormField(field, value)
                },
                enabled = !isSaving
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    enabled = !isSaving
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        isSaving = true
                        viewModel.saveAccount(
                            onSuccess = {
                                isSaving = false
                                onDismiss()
                            },
                            onError = { error ->
                                isSaving = false
                                // Error is already set in formState by viewModel
                            }
                        )
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Save Changes")
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        val transactionCount by viewModel.getTransactionCount(accountToEdit.id).collectAsState()

        DeleteAccountDialog(
            accountToDelete = accountToEdit,
            transactionCount = transactionCount,
            availableAccounts = accounts,
            onConfirm = { replacementAccountId ->
                viewModel.deleteAccount(
                    accountId = accountToEdit.id,
                    replacementAccountId = replacementAccountId,
                    onSuccess = {
                        showDeleteDialog = false
                        onDismiss()
                    },
                    onError = { error ->
                        showDeleteDialog = false
                        // Show error (could add a snackbar here)
                    }
                )
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}
