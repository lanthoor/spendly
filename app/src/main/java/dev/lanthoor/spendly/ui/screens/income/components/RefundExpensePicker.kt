package dev.lanthoor.spendly.ui.screens.income.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.CaretDown
import com.adamglin.phosphoricons.regular.X
import dev.lanthoor.spendly.R
import dev.lanthoor.spendly.domain.model.Expense
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Picker for selecting an expense to link for refunds.
 * Shows when income source is REFUND.
 *
 * @param selectedExpenseId Currently selected expense ID (nullable)
 * @param expenses List of all expenses available for linking
 * @param onExpenseSelected Callback when an expense is selected
 * @param onClear Callback to clear the selection
 * @param label Label for the field
 * @param modifier Optional modifier
 * @param enabled Whether the field is enabled
 */
@Composable
fun RefundExpensePicker(
    selectedExpenseId: Long?,
    expenses: List<Expense>,
    onExpenseSelected: (Long?) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    enabled: Boolean = true
) {
    val displayLabel = label ?: stringResource(R.string.placeholder_select_expense)
    var showDialog by remember { mutableStateOf(false) }
    val selectedExpense = selectedExpenseId?.let { id ->
        expenses.find { it.id == id }
    }

    OutlinedTextField(
        value = selectedExpense?.let {
            "${it.description} - ${it.displayAmount()}"
        } ?: "",
        onValueChange = { /* Read-only */ },
        label = { Text(displayLabel) },
        placeholder = { Text(stringResource(R.string.placeholder_select_expense)) },
        trailingIcon = {
            Row {
                if (selectedExpense != null) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.X,
                        contentDescription = stringResource(R.string.cd_clear_selection),
                        modifier = Modifier
                            .clickable { onClear() }
                            .padding(4.dp)
                    )
                }
                Icon(
                    imageVector = PhosphorIcons.Regular.CaretDown,
                    contentDescription = stringResource(R.string.cd_select_expense)
                )
            }
        },
        readOnly = true,
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { showDialog = true },
        enabled = enabled,
        singleLine = true
    )

    if (showDialog) {
        ExpensePickerDialog(
            expenses = expenses,
            selectedExpenseId = selectedExpenseId,
            onExpenseSelected = { expenseId ->
                onExpenseSelected(expenseId)
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
private fun ExpensePickerDialog(
    expenses: List<Expense>,
    selectedExpenseId: Long?,
    onExpenseSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.title_select_expense_for_refund)) },
        text = {
            if (expenses.isEmpty()) {
                Text(
                    text = stringResource(R.string.msg_no_expenses_available),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(expenses) { expense ->
                        ExpensePickerItem(
                            expense = expense,
                            isSelected = expense.id == selectedExpenseId,
                            dateFormatter = dateFormatter,
                            onClick = { onExpenseSelected(expense.id) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.button_cancel))
            }
        }
    )
}

@Composable
private fun ExpensePickerItem(
    expense: Expense,
    isSelected: Boolean,
    dateFormatter: SimpleDateFormat,
    onClick: () -> Unit
) {
    if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = expense.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = dateFormatter.format(Date(expense.date)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = expense.displayAmount(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
