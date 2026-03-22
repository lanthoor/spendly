package dev.lanthoor.spendly.ui.screens.income.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import dev.lanthoor.spendly.R
import dev.lanthoor.spendly.domain.model.Account
import dev.lanthoor.spendly.domain.model.Category
import dev.lanthoor.spendly.ui.components.AccountDropdown
import dev.lanthoor.spendly.ui.components.AmountTextField
import dev.lanthoor.spendly.ui.components.CategoryDropdown
import dev.lanthoor.spendly.ui.components.DatePickerField
import dev.lanthoor.spendly.ui.components.TimeField
import dev.lanthoor.spendly.ui.screens.income.IncomeFormField
import dev.lanthoor.spendly.ui.screens.income.IncomeFormState

/**
 * Reusable form fields for adding and editing income.
 * Used by both AddIncomeScreen and EditIncomeScreen.
 *
 * @param formState Current form state
 * @param categories List of income categories to display
 * @param selectedCategory Currently selected category
 * @param accounts List of accounts to display
 * @param onFieldChange Callback when any field changes
 * @param modifier Optional modifier
 * @param enabled Whether fields are enabled
 */
@Composable
fun IncomeFormFields(
    formState: IncomeFormState,
    categories: List<Category>,
    selectedCategory: Category?,
    accounts: List<Account>,
    onFieldChange: (IncomeFormField, Any) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Column(modifier = modifier) {
        // Amount field
        AmountTextField(
            value = formState.amount,
            onValueChange = { onFieldChange(IncomeFormField.AMOUNT, it) },
            label = stringResource(R.string.label_amount),
            modifier = Modifier.fillMaxWidth(),
            isError = formState.amountError != null,
            errorMessage = formState.amountError,
            enabled = enabled
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Category dropdown (2-column grid selector)
        CategoryDropdown(
            selectedCategory = selectedCategory,
            categories = categories,
            onCategorySelected = { category ->
                category?.let { onFieldChange(IncomeFormField.CATEGORY, it) }
            },
            label = stringResource(R.string.label_category),
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Date and Time fields (side by side)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DatePickerField(
                selectedDate = formState.date,
                onDateSelected = { onFieldChange(IncomeFormField.DATE, it) },
                label = stringResource(R.string.label_date),
                modifier = Modifier.weight(1f),
                enabled = enabled
            )

            TimeField(
                selectedTime = formState.date,
                onTimeSelected = { onFieldChange(IncomeFormField.DATE, it) },
                label = stringResource(R.string.label_time),
                modifier = Modifier.weight(1f),
                enabled = enabled
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Description field
        OutlinedTextField(
            value = formState.description,
            onValueChange = { onFieldChange(IncomeFormField.DESCRIPTION, it) },
            label = { Text(stringResource(R.string.label_description)) },
            modifier = Modifier.fillMaxWidth(),
            isError = formState.descriptionError != null,
            supportingText = if (formState.descriptionError != null) {
                { Text(formState.descriptionError) }
            } else {
                {
                    Text(
                        stringResource(
                            R.string.label_description_counter,
                            formState.description.length
                        )
                    )
                }
            },
            enabled = enabled,
            minLines = 2,
            maxLines = 4,
            singleLine = false,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    onSave()
                }
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Account dropdown
        val selectedAccount = remember(formState.accountId, accounts) {
            accounts.find { it.id == formState.accountId }
        }
        AccountDropdown(
            selectedAccount = selectedAccount,
            accounts = accounts,
            onAccountSelected = { account ->
                onFieldChange(IncomeFormField.ACCOUNT_ID, account.id)
            },
            label = stringResource(R.string.label_account),
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled
        )
    }
}
