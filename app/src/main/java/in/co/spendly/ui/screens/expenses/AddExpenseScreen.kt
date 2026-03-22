package `in`.co.spendly.ui.screens.expenses

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Info
import `in`.co.spendly.R
import `in`.co.spendly.ui.components.LoadingIndicator
import `in`.co.spendly.ui.screens.expenses.components.ExpenseFormFields
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
    viewModel: ExpenseViewModel = hiltViewModel(),
    resetTrigger: Any? = null
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
            message = stringResource(R.string.label_saving_expense),
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
                text = stringResource(R.string.screen_add_expense_title),
                style = MaterialTheme.typography.headlineSmall,
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
                    onSave = {
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
                            text = stringResource(R.string.msg_save_receipts_first),
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding(),
                    enabled = !formState.isSubmitting
                ) {
                    Text(stringResource(R.string.button_save_expense))
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
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AddExpenseScreenPreview() {
    // Preview would require mocked ViewModel - skipping for now
    Text(stringResource(R.string.screen_add_expense_title))
}
