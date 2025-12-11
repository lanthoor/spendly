package `in`.mylullaby.spendly.ui.screens.expenses.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import `in`.mylullaby.spendly.domain.model.Category
import `in`.mylullaby.spendly.domain.model.Expense
import `in`.mylullaby.spendly.ui.components.IconMapper
import `in`.mylullaby.spendly.utils.PaymentMethod
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * List item displaying expense information.
 * Shows category icon, description, date/payment method, and amount.
 *
 * @param expense Expense to display
 * @param category Category for the expense (nullable for uncategorized)
 * @param onClick Callback when item is clicked
 * @param modifier Optional modifier
 */
@Composable
fun ExpenseListItem(
    expense: Expense,
    category: Category?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val formattedDate = dateFormatter.format(Date(expense.date))

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category icon
            Icon(
                imageVector = IconMapper.getIcon(category?.icon ?: "category"),
                contentDescription = category?.name ?: "Uncategorized",
                tint = if (category != null) Color(category.color) else Color.Gray,
                modifier = Modifier.padding(end = 16.dp)
            )

            // Description and details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = expense.description,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = " • ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = expense.paymentMethod.name.replace('_', ' '),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Amount
            Text(
                text = expense.displayAmount(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        HorizontalDivider()
    }
}

@Preview(showBackground = true)
@Composable
private fun ExpenseListItemPreview() {
    val sampleExpense = Expense(
        id = 1,
        amount = 10050, // 100.50 in paise
        categoryId = 1,
        date = System.currentTimeMillis(),
        description = "Lunch at restaurant",
        paymentMethod = PaymentMethod.UPI,
        createdAt = System.currentTimeMillis(),
        modifiedAt = System.currentTimeMillis()
    )

    val sampleCategory = Category(
        id = 1,
        name = "Food & Dining",
        icon = "restaurant",
        color = 0xFFFF6B6B.toInt(),
        isCustom = false,
        sortOrder = 1
    )

    ExpenseListItem(
        expense = sampleExpense,
        category = sampleCategory,
        onClick = {}
    )
}
