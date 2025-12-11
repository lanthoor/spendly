package `in`.mylullaby.spendly.ui.screens.income

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Trash
import `in`.mylullaby.spendly.ui.components.LoadingIndicator
import `in`.mylullaby.spendly.ui.screens.expenses.components.DeleteConfirmDialog
import `in`.mylullaby.spendly.ui.screens.income.components.IncomeFormFields
import `in`.mylullaby.spendly.ui.screens.income.components.RefundExpensePicker
import `in`.mylullaby.spendly.utils.IncomeSource
import kotlinx.coroutines.launch

/**
 * Screen for editing an existing income (modal bottom sheet).
 * Uses IncomeViewModel and IncomeFormFields component.
 *
 * @param incomeId ID of the income to edit
 * @param viewModel IncomeViewModel instance (injected by Hilt or passed from parent)
 * @param onDismiss Callback when user dismisses the sheet
 * @param onSuccess Callback when income is successfully updated
 * @param onDelete Callback when income is successfully deleted
 */
@Composable
fun EditIncomeScreen(
    incomeId: Long,
    viewModel: IncomeViewModel = hiltViewModel(),
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
    onDelete: () -> Unit
) {
    val formState by viewModel.formState.collectAsState()
    val incomeCategories by viewModel.incomeCategories.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var showDeleteDialog by remember { mutableStateOf(false) }

    // Load income when screen opens
    LaunchedEffect(incomeId) {
        viewModel.loadIncomeById(incomeId)
    }

    // Show delete confirmation dialog
    if (showDeleteDialog) {
        DeleteConfirmDialog(
            onConfirm = {
                coroutineScope.launch {
                    val result = viewModel.deleteIncome(incomeId)
                    if (result.isSuccess) {
                        onDelete()
                    }
                }
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    // Show error if income not found
    if (formState.submitError != null && !formState.isEditMode) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Error: ${formState.submitError}",
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onDismiss) {
                Text("Go Back")
            }
        }
        return
    }

    if (formState.isSubmitting) {
        LoadingIndicator(
            message = "Saving changes...",
            modifier = Modifier
                .fillMaxWidth()
                .padding(64.dp)
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
                    text = "Edit Income",
                    style = MaterialTheme.typography.headlineSmall
                )
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.Trash,
                        contentDescription = "Delete income"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column {
                IncomeFormFields(
                    formState = formState,
                    categories = incomeCategories,
                    selectedCategory = formState.selectedCategory,
                    accounts = accounts,
                    onFieldChange = { field, value ->
                        viewModel.updateFormField(field, value)
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // Show refund expense picker when source is REFUND
                if (formState.source == IncomeSource.REFUND) {
                    Spacer(modifier = Modifier.height(16.dp))
                    RefundExpensePicker(
                        selectedExpenseId = formState.linkedExpenseId,
                        expenses = expenses,
                        onExpenseSelected = { expenseId ->
                            viewModel.updateFormField(IncomeFormField.LINKED_EXPENSE_ID, expenseId ?: 0L)
                        },
                        onClear = {
                            viewModel.updateFormField(IncomeFormField.LINKED_EXPENSE_ID, 0L)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Update button
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val result = viewModel.saveIncome()
                            if (result.isSuccess) {
                                onSuccess()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !formState.isSubmitting
                ) {
                    Text("Update Income")
                }

                // Show submit error if any
                formState.submitError?.let { error ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Bottom padding for sheet
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
