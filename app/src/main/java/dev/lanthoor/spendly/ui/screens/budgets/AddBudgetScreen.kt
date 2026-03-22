package dev.lanthoor.spendly.ui.screens.budgets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.lanthoor.spendly.R
import dev.lanthoor.spendly.ui.components.FormActionButtons
import dev.lanthoor.spendly.ui.screens.budgets.components.BudgetFormFields

/**
 * Bottom sheet screen for adding a new budget.
 * Uses shared BudgetFormFields component and BudgetViewModel.
 *
 * @param viewModel BudgetViewModel instance (shared from parent)
 * @param onDismiss Callback when user cancels
 * @param onSuccess Callback when budget is added successfully
 */
@Composable
fun AddBudgetScreen(
    viewModel: BudgetViewModel,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val formState by viewModel.formState.collectAsState()
    val expenseCategories by viewModel.expenseCategories.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(bottom = 32.dp)
    ) {
        // Header
        Text(
            text = stringResource(R.string.title_add_budget),
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

        // Save button
        FormActionButtons(
            onSave = { viewModel.saveBudget(onSuccess) },
            isSaving = formState.isSubmitting,
            saveLabel = stringResource(R.string.button_save_budget)
        )
    }
}
