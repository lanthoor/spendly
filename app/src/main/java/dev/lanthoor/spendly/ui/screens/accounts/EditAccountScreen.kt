package dev.lanthoor.spendly.ui.screens.accounts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Trash
import dev.lanthoor.spendly.R
import dev.lanthoor.spendly.domain.model.Account
import dev.lanthoor.spendly.ui.components.FormActionButtons
import dev.lanthoor.spendly.ui.screens.accounts.components.AccountFormFields
import dev.lanthoor.spendly.ui.screens.accounts.components.DeleteAccountDialog

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
                    text = stringResource(R.string.screen_edit_account_title),
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
                            contentDescription = stringResource(R.string.cd_delete_account)
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
                enabled = !formState.isSubmitting
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            FormActionButtons(
                onSave = {
                    viewModel.saveAccount(
                        onSuccess = { onDismiss() },
                        onError = { /* Error is already set in formState by viewModel */ }
                    )
                },
                onCancel = onDismiss,
                isSaving = formState.isSubmitting,
                saveLabel = stringResource(R.string.button_save_changes),
                modifier = Modifier.imePadding()
            )
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
