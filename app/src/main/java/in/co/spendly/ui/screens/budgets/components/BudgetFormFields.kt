package `in`.co.spendly.ui.screens.budgets.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import `in`.co.spendly.R
import `in`.co.spendly.domain.model.Category
import `in`.co.spendly.ui.components.AmountTextField
import `in`.co.spendly.ui.screens.budgets.BudgetFormState

/**
 * Reusable form fields for budget creation and editing.
 * Budget is valid from current month onwards.
 *
 * @param formState Current form state
 * @param categories List of expense categories
 * @param onAmountChange Callback when amount changes
 * @param onCategoryChange Callback when category changes
 * @param modifier Modifier for the column
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetFormFields(
    formState: BudgetFormState,
    categories: List<Category>,
    onAmountChange: (String) -> Unit,
    onCategoryChange: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    var categoryExpanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        // Amount field
        AmountTextField(
            value = formState.amount,
            onValueChange = onAmountChange,
            label = stringResource(R.string.label_budget_amount),
            isError = formState.amountError != null,
            errorMessage = formState.amountError,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Category dropdown (with Overall Budget option)
        ExposedDropdownMenuBox(
            expanded = categoryExpanded,
            onExpandedChange = { categoryExpanded = it }
        ) {
            OutlinedTextField(
                value = if (formState.categoryId == null) {
                    stringResource(R.string.label_overall_budget)
                } else {
                    categories.find { it.id == formState.categoryId }?.name
                        ?: stringResource(R.string.label_select_category)
                },
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.label_category)) },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
            )

            ExposedDropdownMenu(
                expanded = categoryExpanded,
                onDismissRequest = { categoryExpanded = false }
            ) {
                // Overall Budget option
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.label_overall_budget)) },
                    onClick = {
                        onCategoryChange(null)
                        categoryExpanded = false
                    }
                )

                // Category options
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category.name) },
                        onClick = {
                            onCategoryChange(category.id)
                            categoryExpanded = false
                        }
                    )
                }
            }
        }

        // Error message
        if (formState.submitError != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formState.submitError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
