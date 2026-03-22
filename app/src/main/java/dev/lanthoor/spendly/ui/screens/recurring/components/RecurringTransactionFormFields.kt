package dev.lanthoor.spendly.ui.screens.recurring.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.lanthoor.spendly.R
import dev.lanthoor.spendly.domain.model.Account
import dev.lanthoor.spendly.domain.model.Category
import dev.lanthoor.spendly.ui.components.AccountDropdown
import dev.lanthoor.spendly.ui.components.AmountTextField
import dev.lanthoor.spendly.ui.components.CategoryDropdown
import dev.lanthoor.spendly.ui.components.DatePickerField
import dev.lanthoor.spendly.ui.screens.recurring.RecurringTransactionFormField
import dev.lanthoor.spendly.ui.screens.recurring.RecurringTransactionFormState

/**
 * Reusable form fields component for recurring transaction add/edit screens.
 *
 * @param formState Current form state
 * @param categories Filtered list of categories based on transaction type
 * @param accounts List of all accounts
 * @param onFieldChange Callback when a field value changes
 * @param modifier Modifier for the component
 */
@Composable
fun RecurringTransactionFormFields(
    formState: RecurringTransactionFormState,
    categories: List<Category>,
    accounts: List<Account>,
    onFieldChange: (RecurringTransactionFormField, Any) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Transaction Type Selection
        Text(
            text = stringResource(R.string.label_transaction_type),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        TransactionTypeSelectionButton(
            selectedType = formState.transactionType,
            onTypeSelected = { type ->
                onFieldChange(RecurringTransactionFormField.TRANSACTION_TYPE, type)
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Amount
        AmountTextField(
            value = formState.amount,
            onValueChange = { amount ->
                onFieldChange(RecurringTransactionFormField.AMOUNT, amount)
            },
            isError = formState.amountError != null,
            errorMessage = formState.amountError,
            modifier = Modifier.fillMaxWidth()
        )

        // Category
        CategoryDropdown(
            selectedCategory = categories.find { it.id == formState.categoryId },
            categories = categories,
            onCategorySelected = { category ->
                onFieldChange(RecurringTransactionFormField.CATEGORY, category?.id ?: 0L)
            },
            label = stringResource(R.string.label_category_optional),
            modifier = Modifier.fillMaxWidth()
        )

        // Account
        AccountDropdown(
            selectedAccount = accounts.find { it.id == formState.accountId },
            accounts = accounts,
            onAccountSelected = { account ->
                onFieldChange(RecurringTransactionFormField.ACCOUNT, account.id)
            },
            modifier = Modifier.fillMaxWidth()
        )

        // Description
        OutlinedTextField(
            value = formState.description,
            onValueChange = { desc ->
                onFieldChange(RecurringTransactionFormField.DESCRIPTION, desc)
            },
            label = { Text(stringResource(R.string.label_description)) },
            isError = formState.descriptionError != null,
            supportingText = formState.descriptionError?.let { { Text(it) } },
            singleLine = false,
            maxLines = 3,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier.fillMaxWidth()
        )

        // Frequency Selection
        Text(
            text = stringResource(R.string.label_frequency),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        FrequencySelectionButton(
            selectedFrequency = formState.frequency,
            onFrequencySelected = { frequency ->
                onFieldChange(RecurringTransactionFormField.FREQUENCY, frequency)
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Next Occurrence Date
        DatePickerField(
            selectedDate = formState.nextDate,
            onDateSelected = { date ->
                onFieldChange(RecurringTransactionFormField.NEXT_DATE, date)
            },
            label = stringResource(R.string.label_next_occurrence),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
