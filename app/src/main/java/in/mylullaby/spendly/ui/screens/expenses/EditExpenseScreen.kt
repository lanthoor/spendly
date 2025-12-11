package `in`.mylullaby.spendly.ui.screens.expenses

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Trash
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import `in`.mylullaby.spendly.ui.components.LoadingIndicator
import `in`.mylullaby.spendly.ui.components.SpendlyTopAppBar
import `in`.mylullaby.spendly.ui.screens.expenses.components.DeleteConfirmDialog
import `in`.mylullaby.spendly.ui.screens.expenses.components.ExpenseFormFields
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
    var showDeleteDialog by remember { mutableStateOf(false) }

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
}

@Preview(showBackground = true)
@Composable
private fun EditExpenseScreenPreview() {
    // Preview would require mocked ViewModel - skipping for now
    Text("EditExpenseScreen Preview")
}
