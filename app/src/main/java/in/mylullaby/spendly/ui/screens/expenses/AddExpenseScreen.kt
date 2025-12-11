package `in`.mylullaby.spendly.ui.screens.expenses

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import `in`.mylullaby.spendly.ui.components.LoadingIndicator
import `in`.mylullaby.spendly.ui.components.SpendlyTopAppBar
import `in`.mylullaby.spendly.ui.screens.expenses.components.ExpenseFormFields
import kotlinx.coroutines.launch

/**
 * Screen for adding a new expense.
 * Uses ExpenseViewModel and ExpenseFormFields component.
 *
 * @param onNavigateBack Callback when user navigates back, receives success/error message or null
 * @param viewModel ExpenseViewModel instance (injected by Hilt)
 */
@Composable
fun AddExpenseScreen(
    onNavigateBack: (String?) -> Unit,
    viewModel: ExpenseViewModel = hiltViewModel()
) {
    val formState by viewModel.formState.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // Reset form when screen opens
    LaunchedEffect(Unit) {
        viewModel.resetForm()
    }

    if (formState.isSubmitting) {
        LoadingIndicator(
            message = "Saving expense...",
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
                text = "Add Expense",
                style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Column {
                ExpenseFormFields(
                    formState = formState,
                    categories = categories,
                    accounts = accounts,
                    onFieldChange = { field, value ->
                        viewModel.updateFormField(field, value)
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Info card about receipts
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.Top
                    ) {
                        Icon(
                            imageVector = PhosphorIcons.Regular.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Save the expense first to attach receipts. You can add photos or PDFs after saving.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Save button
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val result = viewModel.saveExpense()
                            if (result.isSuccess) {
                                onNavigateBack("Expense added successfully")
                            } else {
                                onNavigateBack(
                                    result.exceptionOrNull()?.message ?: "Failed to add expense"
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !formState.isSubmitting
                ) {
                    Text("Save Expense")
                }

                // Show submit error if any
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
private fun AddExpenseScreenPreview() {
    // Preview would require mocked ViewModel - skipping for now
    Text("AddExpenseScreen Preview")
}
