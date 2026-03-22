package dev.lanthoor.spendly.ui.screens.expenses.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.lanthoor.spendly.R
import dev.lanthoor.spendly.domain.model.Account
import dev.lanthoor.spendly.domain.model.Category
import dev.lanthoor.spendly.ui.components.AccountDropdown
import dev.lanthoor.spendly.ui.components.AmountTextField
import dev.lanthoor.spendly.ui.components.CategoryDropdown
import dev.lanthoor.spendly.ui.components.DatePickerField
import dev.lanthoor.spendly.ui.components.TimeField
import dev.lanthoor.spendly.ui.screens.expenses.ExpenseFormState
import dev.lanthoor.spendly.ui.screens.expenses.FormField
import dev.lanthoor.spendly.utils.AccountType

/**
 * Reusable form fields for adding and editing expenses.
 * Used by both AddExpenseScreen and EditExpenseScreen.
 *
 * @param formState Current form state
 * @param categories List of available categories
 * @param accounts List of available accounts
 * @param onFieldChange Callback when any field changes
 * @param modifier Optional modifier
 * @param enabled Whether fields are enabled
 */
@Composable
fun ExpenseFormFields(
    formState: ExpenseFormState,
    categories: List<Category>,
    accounts: List<Account>,
    onFieldChange: (FormField, Any) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Column(modifier = modifier) {
        // Amount field
        AmountTextField(
            value = formState.amount,
            onValueChange = { onFieldChange(FormField.AMOUNT, it) },
            label = stringResource(R.string.label_amount),
            modifier = Modifier.fillMaxWidth(),
            isError = formState.amountError != null,
            errorMessage = formState.amountError,
            enabled = enabled
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Category dropdown
        val selectedCategory = remember(formState.categoryId, categories) {
            categories.find { it.id == formState.categoryId }
        }
        CategoryDropdown(
            selectedCategory = selectedCategory,
            categories = categories,
            onCategorySelected = { category ->
                onFieldChange(FormField.CATEGORY_ID, category?.id ?: Category.OTHERS_CATEGORY_ID)
            },
            label = stringResource(R.string.label_category_optional),
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
                onDateSelected = { onFieldChange(FormField.DATE, it) },
                label = stringResource(R.string.label_date),
                modifier = Modifier.weight(1f),
                enabled = enabled
            )

            TimeField(
                selectedTime = formState.date,
                onTimeSelected = { onFieldChange(FormField.DATE, it) },
                label = stringResource(R.string.label_time),
                modifier = Modifier.weight(1f),
                enabled = enabled
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Description field
        OutlinedTextField(
            value = formState.description,
            onValueChange = { onFieldChange(FormField.DESCRIPTION, it) },
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
                onFieldChange(FormField.ACCOUNT_ID, account.id)
            },
            label = stringResource(R.string.label_account),
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ExpenseFormFieldsPreview() {
    val sampleCategories = listOf(
        Category(1, "Food", "restaurant", 0xFFFF6B6B.toInt(), false, 1),
        Category(2, "Travel", "flight", 0xFF4ECDC4.toInt(), false, 2)
    )

    val sampleAccounts = listOf(
        Account(
            id = 1,
            name = "My Account",
            type = AccountType.BANK,
            icon = "bank",
            color = 0xFF00BFA5.toInt(),
            isCustom = false,
            sortOrder = 1,
            createdAt = 0,
            modifiedAt = 0
        )
    )

    ExpenseFormFields(
        formState = ExpenseFormState(
            amount = "100.50",
            categoryId = 1,
            description = "Lunch at restaurant",
            accountId = 1
        ),
        categories = sampleCategories,
        accounts = sampleAccounts,
        onFieldChange = { _, _ -> },
        onSave = { },
        modifier = Modifier.padding(16.dp)
    )
}
