package `in`.co.spendly.ui.screens.budgets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Trash
import `in`.co.spendly.R
import `in`.co.spendly.ui.screens.budgets.components.BudgetFormFields

/**
 * Bottom sheet screen for editing an existing budget.
 * Includes delete button with confirmation dialog.
 * Uses shared BudgetFormFields component and BudgetViewModel.
 *
 * @param budgetId ID of the budget to edit
 * @param viewModel BudgetViewModel instance (shared from parent)
 * @param onDismiss Callback when user cancels
 * @param onSuccess Callback when budget is updated successfully
 * @param onDelete Callback when budget is deleted successfully
 */
@Composable
fun EditBudgetScreen(
    budgetId: Long,
    viewModel: BudgetViewModel,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
    onDelete: () -> Unit
) {
    val formState by viewModel.formState.collectAsState()
    val expenseCategories by viewModel.expenseCategories.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Load budget when screen opens
    LaunchedEffect(budgetId) {
        viewModel.loadBudgetById(budgetId)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(bottom = 32.dp)
    ) {
        // Header
        Text(
            text = stringResource(R.string.screen_edit_budget_title),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Form fields
        BudgetFormFields(
            formState = formState,
            categories = expenseCategories,
            onAmountChange = viewModel::updateAmount,
            onCategoryChange = viewModel::updateCategory
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Action buttons row
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Delete button
            OutlinedButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = PhosphorIcons.Regular.Trash,
                    contentDescription = stringResource(R.string.button_delete),
                    modifier = Modifier.padding(end = 4.dp)
                )
                Text(stringResource(R.string.button_delete))
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Save button
            Button(
                onClick = { viewModel.saveBudget(onSuccess) },
                enabled = !formState.isSubmitting,
                modifier = Modifier.weight(1f)
            ) {
                if (formState.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .height(24.dp)
                            .padding(end = 8.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Text(
                    if (formState.isSubmitting) stringResource(R.string.label_saving) else stringResource(
                        R.string.button_save
                    )
                )
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.dialog_delete_budget_title)) },
            text = {
                Text(
                    stringResource(R.string.dialog_delete_budget_title) + "\n" + stringResource(
                        R.string.msg_delete_budget_confirm
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteBudget(budgetId, onDelete)
                    }
                ) {
                    Text(
                        stringResource(R.string.button_delete),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.button_cancel))
                }
            }
        )
    }
}
