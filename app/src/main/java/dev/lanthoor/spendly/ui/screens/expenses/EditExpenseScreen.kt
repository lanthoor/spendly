package dev.lanthoor.spendly.ui.screens.expenses

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ChatText
import com.adamglin.phosphoricons.regular.Plus
import com.adamglin.phosphoricons.regular.Trash
import dev.lanthoor.spendly.R
import dev.lanthoor.spendly.ui.components.LoadingIndicator
import dev.lanthoor.spendly.ui.screens.expenses.components.CameraCapture
import dev.lanthoor.spendly.ui.screens.expenses.components.DeleteConfirmDialog
import dev.lanthoor.spendly.ui.screens.expenses.components.ExpenseFormFields
import dev.lanthoor.spendly.ui.screens.expenses.components.ReceiptPickerSheet
import dev.lanthoor.spendly.ui.screens.expenses.components.ReceiptThumbnail
import dev.lanthoor.spendly.utils.PermissionUtils
import kotlinx.coroutines.launch

/**
 * Screen for editing an existing expense.
 * Uses ExpenseViewModel and ExpenseFormFields component.
 *
 * @param expenseId ID of the expense to edit
 * @param onNavigateBack Callback when user navigates back, receives success/error message or null
 * @param viewModel ExpenseViewModel instance (injected by Hilt)
 */
@Composable
fun EditExpenseScreen(
    expenseId: Long,
    onNavigateBack: (String?) -> Unit,
    viewModel: ExpenseViewModel = hiltViewModel()
) {
    val formState by viewModel.formState.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Preload localized messages used inside callbacks
    val msgExpenseDeleted = stringResource(R.string.msg_expense_deleted)
    val msgFailedDeleteExpense = stringResource(R.string.msg_failed_delete_expense)
    val msgExpenseUpdatedSuccess = stringResource(R.string.msg_expense_updated_success)
    val msgFailedSaveChanges = stringResource(R.string.msg_failed_save_changes)

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showReceiptPicker by remember { mutableStateOf(false) }
    var showCamera by remember { mutableStateOf(false) }

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            coroutineScope.launch {
                viewModel.addReceipt(context, expenseId, uri)
            }
        }
    }

    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showCamera = true
        }
    }

    // Load expense when screen opens
    LaunchedEffect(expenseId) {
        viewModel.loadExpenseById(expenseId)
    }

    // Show delete confirmation dialog
    if (showDeleteDialog) {
        DeleteConfirmDialog(
            onConfirm = {
                coroutineScope.launch {
                    val result = viewModel.deleteExpense(expenseId)
                    if (result.isSuccess) {
                        onNavigateBack(msgExpenseDeleted)
                    } else {
                        onNavigateBack(
                            result.exceptionOrNull()?.message ?: msgFailedDeleteExpense
                        )
                    }
                }
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    // Show error if expense not found
    if (formState.submitError != null && !formState.isEditMode) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(
                    R.string.label_error_with_msg,
                    formState.submitError?.toString() ?: ""
                ),
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { onNavigateBack(null) }) {
                Text(stringResource(R.string.button_go_back))
            }
        }
        return
    }

    if (formState.isSubmitting) {
        LoadingIndicator(
            message = stringResource(R.string.label_saving),
            modifier = Modifier.fillMaxWidth()
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Header with title and delete button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.screen_edit_expense_title),
                    style = MaterialTheme.typography.headlineSmall
                )
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.Trash,
                        contentDescription = stringResource(R.string.cd_delete_expense)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            ExpenseFormFields(
                formState = formState,
                categories = categories,
                accounts = accounts,
                onFieldChange = { field, value ->
                    viewModel.updateFormField(field, value)
                },
                onSave = {
                    coroutineScope.launch {
                        val result = viewModel.saveExpense()
                        if (result.isSuccess) {
                            onNavigateBack(msgExpenseUpdatedSuccess)
                        } else {
                            onNavigateBack(
                                result.exceptionOrNull()?.message ?: msgFailedSaveChanges
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // SMS Source Section (if transaction was created from SMS)
            if (formState.smsBody != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = PhosphorIcons.Regular.ChatText,
                                contentDescription = stringResource(R.string.cd_created_from_sms),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = stringResource(R.string.cd_created_from_sms),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = formState.smsBody!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Receipts Section (only for manually created expenses, not SMS-detected)
            if (formState.smsBody == null) {
                Text(
                    text = stringResource(R.string.header_receipts),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Existing receipts
                    items(formState.receipts) { receipt ->
                        ReceiptThumbnail(
                            receipt = receipt,
                            onDelete = {
                                coroutineScope.launch {
                                    viewModel.deleteReceipt(context, receipt)
                                }
                            },
                            onClick = {
                                // TODO: Full-screen receipt viewer (future enhancement)
                            }
                        )
                    }

                    // Add receipt button
                    item {
                        OutlinedCard(
                            onClick = { showReceiptPicker = true },
                            modifier = Modifier.size(120.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = PhosphorIcons.Regular.Plus,
                                        contentDescription = stringResource(R.string.cd_add_receipt),
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = stringResource(R.string.header_add_receipt),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                }

                // Receipt error
                formState.receiptError?.let { error ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Save button
            Button(
                onClick = {
                    coroutineScope.launch {
                        val result = viewModel.saveExpense()
                        if (result.isSuccess) {
                            onNavigateBack(msgExpenseUpdatedSuccess)
                        } else {
                            onNavigateBack(
                                result.exceptionOrNull()?.message ?: msgFailedSaveChanges
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding(),
                enabled = !formState.isSubmitting
            ) {
                Text(stringResource(R.string.button_save_changes))
            }

            // Show submit error if any
            if (formState.isEditMode) {
                formState.submitError?.let { error ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }

    // Receipt picker sheet
    if (showReceiptPicker) {
        ReceiptPickerSheet(
            onDismiss = { showReceiptPicker = false },
            onSelectFromFiles = {
                filePickerLauncher.launch("*/*")
            },
            onCapturePhoto = {
                if (PermissionUtils.hasCameraPermission(context)) {
                    showCamera = true
                } else {
                    cameraPermissionLauncher.launch(PermissionUtils.CAMERA_PERMISSION)
                }
            }
        )
    }

    // Camera capture
    if (showCamera) {
        CameraCapture(
            onPhotoCaptured = { uri ->
                showCamera = false
                coroutineScope.launch {
                    val result = viewModel.addReceipt(context, expenseId, uri)
                    if (result.isFailure) {
                        // Error is already set in formState by viewModel
                        android.util.Log.e(
                            "EditExpenseScreen",
                            "Failed to add receipt: ${result.exceptionOrNull()?.message}"
                        )
                    }
                }
            },
            onDismiss = { showCamera = false }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EditExpenseScreenPreview() {
    // Preview would require mocked ViewModel - skipping for now
    Text(stringResource(R.string.screen_edit_expense_title))
}
