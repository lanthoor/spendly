package `in`.mylullaby.spendly.ui.screens.expenses.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import `in`.mylullaby.spendly.domain.model.Account
import `in`.mylullaby.spendly.domain.model.Category
import `in`.mylullaby.spendly.ui.components.AccountDropdown
import `in`.mylullaby.spendly.ui.components.AmountTextField
import `in`.mylullaby.spendly.ui.components.CategoryDropdown
import `in`.mylullaby.spendly.ui.components.DatePickerField
import `in`.mylullaby.spendly.ui.screens.expenses.ExpenseFormState
import `in`.mylullaby.spendly.ui.screens.expenses.FormField
import `in`.mylullaby.spendly.utils.AccountType

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
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Column(modifier = modifier) {
        // Amount field
        AmountTextField(
            value = formState.amount,
            onValueChange = { onFieldChange(FormField.AMOUNT, it) },
            label = "Amount",
            modifier = Modifier.fillMaxWidth(),
            isError = formState.amountError != null,
            errorMessage = formState.amountError,
            enabled = enabled
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Category dropdown
        val selectedCategory = formState.categoryId?.let { id ->
            categories.find { it.id == id }
        }
        CategoryDropdown(
            selectedCategory = selectedCategory,
            categories = categories,
            onCategorySelected = { category ->
                onFieldChange(FormField.CATEGORY_ID, category?.id ?: 0L)
            },
            label = "Category (optional)",
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Date picker
        DatePickerField(
            selectedDate = formState.date,
            onDateSelected = { onFieldChange(FormField.DATE, it) },
            label = "Date",
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Description field
        OutlinedTextField(
            value = formState.description,
            onValueChange = { onFieldChange(FormField.DESCRIPTION, it) },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            isError = formState.descriptionError != null,
            supportingText = if (formState.descriptionError != null) {
                { Text(formState.descriptionError) }
            } else {
                { Text("${formState.description.length}/200") }
            },
            enabled = enabled,
            minLines = 2,
            maxLines = 4,
            singleLine = false
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Account dropdown
        val selectedAccount = accounts.find { it.id == formState.accountId }
        AccountDropdown(
            selectedAccount = selectedAccount,
            accounts = accounts,
            onAccountSelected = { account ->
                onFieldChange(FormField.ACCOUNT_ID, account.id)
            },
            label = "Account",
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ExpenseFormFieldsPreview() {
    val sampleCategories = listOf(
        Category(1, "Food & Dining", "restaurant", 0xFFFF6B6B.toInt(), false, 1),
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
        modifier = Modifier.padding(16.dp)
    )
}
