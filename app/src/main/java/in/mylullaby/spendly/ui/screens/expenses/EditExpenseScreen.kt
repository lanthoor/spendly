package `in`.mylullaby.spendly.ui.screens.expenses

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Plus
import com.adamglin.phosphoricons.regular.Trash
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import `in`.mylullaby.spendly.ui.components.LoadingIndicator
import `in`.mylullaby.spendly.ui.components.SpendlyTopAppBar
import `in`.mylullaby.spendly.ui.screens.expenses.components.CameraCapture
import `in`.mylullaby.spendly.ui.screens.expenses.components.DeleteConfirmDialog
import `in`.mylullaby.spendly.ui.screens.expenses.components.ExpenseFormFields
import `in`.mylullaby.spendly.ui.screens.expenses.components.ReceiptPickerSheet
import `in`.mylullaby.spendly.ui.screens.expenses.components.ReceiptThumbnail
import `in`.mylullaby.spendly.utils.PermissionUtils
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
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

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
                        onNavigateBack("Expense deleted")
                    } else {
                        onNavigateBack(
                            result.exceptionOrNull()?.message ?: "Failed to delete expense"
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
                text = "Error: ${formState.submitError}",
                color = androidx.compose.material3.MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { onNavigateBack(null) }) {
                Text("Go Back")
            }
        }
        return
    }

    if (formState.isSubmitting) {
        LoadingIndicator(
            message = "Saving changes...",
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
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(
                    text = "Edit Expense",
                    style = androidx.compose.material3.MaterialTheme.typography.headlineSmall
                )
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.Trash,
                        contentDescription = "Delete expense"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            ExpenseFormFields(
                formState = formState,
                categories = categories,
                onFieldChange = { field, value ->
                    viewModel.updateFormField(field, value)
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Receipts Section
            Text(
                text = "Receipts",
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
                                    contentDescription = "Add receipt",
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Add Receipt",
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

            // Save button
            Button(
                onClick = {
                    coroutineScope.launch {
                        val result = viewModel.saveExpense()
                        if (result.isSuccess) {
                            onNavigateBack("Expense updated successfully")
                        } else {
                            onNavigateBack(
                                result.exceptionOrNull()?.message ?: "Failed to save changes"
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !formState.isSubmitting
            ) {
                Text("Save Changes")
            }

            // Show submit error if any
            if (formState.isEditMode) {
                formState.submitError?.let { error ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall
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
                        android.util.Log.e("EditExpenseScreen", "Failed to add receipt: ${result.exceptionOrNull()?.message}")
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
    Text("EditExpenseScreen Preview")
}
