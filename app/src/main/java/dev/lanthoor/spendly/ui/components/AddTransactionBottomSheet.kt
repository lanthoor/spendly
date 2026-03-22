package dev.lanthoor.spendly.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import dev.lanthoor.spendly.R
import dev.lanthoor.spendly.ui.screens.expenses.ExpenseViewModel
import dev.lanthoor.spendly.ui.screens.expenses.components.ExpenseFormFields
import dev.lanthoor.spendly.ui.screens.income.IncomeViewModel
import dev.lanthoor.spendly.ui.screens.income.components.IncomeFormFields
import dev.lanthoor.spendly.ui.screens.recurring.components.TransactionTypeSelectionButton
import dev.lanthoor.spendly.utils.TransactionType
import kotlinx.coroutines.launch

/**
 * Bottom sheet for adding new transactions with type selector.
 *
 * This component presents a segmented button to switch between Expense and Income,
 * showing the appropriate form fields based on the selected type.
 *
 * @param onDismiss Callback when the bottom sheet is dismissed
 * @param sheetState State for the modal bottom sheet
 * @param expenseViewModel ViewModel for expense operations
 * @param incomeViewModel ViewModel for income operations
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionBottomSheet(
    onDismiss: () -> Unit,
    sheetState: SheetState,
    expenseViewModel: ExpenseViewModel = hiltViewModel(),
    incomeViewModel: IncomeViewModel = hiltViewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }

    // Reset forms when bottom sheet is disposed
    DisposableEffect(Unit) {
        onDispose {
            expenseViewModel.resetForm()
            incomeViewModel.resetForm()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Title
            Text(
                text = stringResource(R.string.title_add_transaction),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Transaction Type Selector
            Text(
                text = stringResource(R.string.label_transaction_type),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            TransactionTypeSelectionButton(
                selectedType = selectedType,
                onTypeSelected = { type ->
                    selectedType = type
                    // Reset forms when switching type
                    expenseViewModel.resetForm()
                    incomeViewModel.resetForm()
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Show appropriate form based on selected type
            when (selectedType) {
                TransactionType.EXPENSE -> {
                    val expenseFormState by expenseViewModel.formState.collectAsStateWithLifecycle()
                    val categories by expenseViewModel.categories.collectAsStateWithLifecycle()
                    val accounts by expenseViewModel.accounts.collectAsStateWithLifecycle()

                    ExpenseFormFields(
                        formState = expenseFormState,
                        categories = categories,
                        accounts = accounts,
                        onFieldChange = { field, value ->
                            expenseViewModel.updateFormField(
                                field,
                                value
                            )
                        },
                        onSave = {
                            coroutineScope.launch {
                                val result = expenseViewModel.saveExpense()
                                if (result.isSuccess) {
                                    onDismiss()
                                }
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    FormActionButtons(
                        onSave = {
                            coroutineScope.launch {
                                val result = expenseViewModel.saveExpense()
                                if (result.isSuccess) {
                                    onDismiss()
                                }
                            }
                        },
                        onCancel = onDismiss,
                        isSaving = expenseFormState.isSubmitting,
                        saveLabel = stringResource(R.string.button_add_expense),
                        enabled = !expenseFormState.isSubmitting
                    )
                }

                TransactionType.INCOME -> {
                    val incomeFormState by incomeViewModel.formState.collectAsStateWithLifecycle()
                    val incomeCategories by incomeViewModel.incomeCategories.collectAsStateWithLifecycle()
                    val accounts by incomeViewModel.accounts.collectAsStateWithLifecycle()

                    IncomeFormFields(
                        formState = incomeFormState,
                        categories = incomeCategories,
                        selectedCategory = incomeFormState.selectedCategory,
                        accounts = accounts,
                        onFieldChange = { field, value ->
                            incomeViewModel.updateFormField(
                                field,
                                value
                            )
                        },
                        onSave = {
                            coroutineScope.launch {
                                val result = incomeViewModel.saveIncome()
                                if (result.isSuccess) {
                                    onDismiss()
                                }
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    FormActionButtons(
                        onSave = {
                            coroutineScope.launch {
                                val result = incomeViewModel.saveIncome()
                                if (result.isSuccess) {
                                    onDismiss()
                                }
                            }
                        },
                        onCancel = onDismiss,
                        isSaving = incomeFormState.isSubmitting,
                        saveLabel = stringResource(R.string.button_add_income),
                        enabled = !incomeFormState.isSubmitting
                    )
                }
            }
        }
    }
}
