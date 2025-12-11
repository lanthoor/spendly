package `in`.mylullaby.spendly.ui.screens.income

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import `in`.mylullaby.spendly.ui.components.LoadingIndicator
import `in`.mylullaby.spendly.ui.screens.income.components.IncomeFormFields
import `in`.mylullaby.spendly.ui.screens.income.components.RefundExpensePicker
import `in`.mylullaby.spendly.utils.IncomeSource
import kotlinx.coroutines.launch

/**
 * Screen for adding a new income (modal bottom sheet).
 * Uses IncomeViewModel and IncomeFormFields component.
 *
 * @param viewModel IncomeViewModel instance (injected by Hilt or passed from parent)
 * @param onDismiss Callback when user dismisses the sheet
 * @param onSuccess Callback when income is successfully saved
 */
@Composable
fun AddIncomeScreen(
    viewModel: IncomeViewModel = hiltViewModel(),
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val formState by viewModel.formState.collectAsState()
    val incomeCategories by viewModel.incomeCategories.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // Reset form when screen opens
    LaunchedEffect(Unit) {
        viewModel.resetForm()
    }

    if (formState.isSubmitting) {
        LoadingIndicator(
            message = "Saving income...",
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
            // Title
            Text(
                text = "Add Income",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

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

                // Save button
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
                    Text("Save Income")
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
