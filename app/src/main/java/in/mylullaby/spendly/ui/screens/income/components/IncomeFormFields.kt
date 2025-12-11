package `in`.mylullaby.spendly.ui.screens.income.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import `in`.mylullaby.spendly.domain.model.Account
import `in`.mylullaby.spendly.domain.model.Category
import `in`.mylullaby.spendly.ui.components.AccountDropdown
import `in`.mylullaby.spendly.ui.components.AmountTextField
import `in`.mylullaby.spendly.ui.components.CategoryDropdown
import `in`.mylullaby.spendly.ui.components.DatePickerField
import `in`.mylullaby.spendly.ui.screens.income.IncomeFormField
import `in`.mylullaby.spendly.ui.screens.income.IncomeFormState

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
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Column(modifier = modifier) {
        // Amount field
        AmountTextField(
            value = formState.amount,
            onValueChange = { onFieldChange(IncomeFormField.AMOUNT, it) },
            label = "Amount",
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
            label = "Category",
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Date picker
        DatePickerField(
            selectedDate = formState.date,
            onDateSelected = { onFieldChange(IncomeFormField.DATE, it) },
            label = "Date",
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Description field
        OutlinedTextField(
            value = formState.description,
            onValueChange = { onFieldChange(IncomeFormField.DESCRIPTION, it) },
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
        AccountDropdown(
            selectedAccount = accounts.find { it.id == formState.accountId },
            accounts = accounts,
            onAccountSelected = { account ->
                onFieldChange(IncomeFormField.ACCOUNT_ID, account.id)
            },
            label = "Account",
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled
        )
    }
}
