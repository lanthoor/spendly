package `in`.co.spendly.ui.screens.recurring

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import `in`.co.spendly.R
import `in`.co.spendly.ui.components.LoadingIndicator
import `in`.co.spendly.ui.screens.expenses.components.DeleteConfirmDialog
import `in`.co.spendly.ui.screens.recurring.components.RecurringTransactionFormFields
import kotlinx.coroutines.launch

/**
 * Screen for editing a recurring transaction.
 *
 * @param recurringTransactionId ID of the recurring transaction to edit
 * @param onNavigateBack Callback when user navigates back, receives success/error message or null
 * @param viewModel RecurringTransactionViewModel instance (injected by Hilt)
 */
@Composable
fun EditRecurringTransactionScreen(
    recurringTransactionId: Long,
    onNavigateBack: (String?) -> Unit,
    viewModel: RecurringTransactionViewModel = hiltViewModel()
) {
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val allCategories by viewModel.allCategories.collectAsStateWithLifecycle()
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Pre-load localized messages to use in coroutine callbacks
    val msgUpdated = stringResource(R.string.msg_recurring_updated_success)
    val msgDeleted = stringResource(R.string.msg_recurring_deleted_success)
    val msgDeleteFailed = stringResource(R.string.msg_recurring_delete_failed)

    // Load recurring transaction on screen open
    LaunchedEffect(recurringTransactionId) {
        viewModel.loadRecurringTransactionById(recurringTransactionId)
    }

    if (formState.isSubmitting) {
        LoadingIndicator(
            message = stringResource(R.string.label_saving_recurring_transaction),
            modifier = Modifier.fillMaxSize()
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Title
            Text(
                text = stringResource(R.string.screen_edit_recurring_transaction_title),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Form Fields
            RecurringTransactionFormFields(
                formState = formState,
                categories = allCategories,
                accounts = accounts,
                onFieldChange = { field, value ->
                    viewModel.updateFormField(field, value)
                }
            )

            // Error Message
            formState.submitError?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                // Delete Button
                Button(
                    onClick = { showDeleteDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.button_delete))
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Cancel Button
                TextButton(
                    onClick = { onNavigateBack(null) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.button_cancel))
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Save Button
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val result = viewModel.saveRecurringTransaction()
                            if (result.isSuccess) {
                                onNavigateBack(msgUpdated)
                            } else {
                                // Error is already shown in submitError
                            }
                        }
                    },
                    enabled = !formState.isSubmitting,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.button_save))
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        DeleteConfirmDialog(
            onConfirm = {
                coroutineScope.launch {
                    val result = viewModel.deleteRecurringTransaction(recurringTransactionId)
                    if (result.isSuccess) {
                        onNavigateBack(msgDeleted)
                    } else {
                        onNavigateBack(msgDeleteFailed)
                    }
                }
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}
