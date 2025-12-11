package `in`.mylullaby.spendly.ui.screens.dashboard.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import `in`.mylullaby.spendly.domain.model.Category
import `in`.mylullaby.spendly.ui.components.IconMapper
import `in`.mylullaby.spendly.ui.screens.dashboard.RecentTransaction
import `in`.mylullaby.spendly.utils.CurrencyUtils
import `in`.mylullaby.spendly.utils.toDisplayName
import `in`.mylullaby.spendly.utils.toDisplayString
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Widget displaying recent transactions (combined expenses and income)
 */
@Composable
fun RecentTransactionsWidget(
    transactions: List<RecentTransaction>,
    categories: List<Category>,
    onTransactionClick: (RecentTransaction) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Text(
                text = "Recent Transactions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            HorizontalDivider()

            Spacer(modifier = Modifier.height(8.dp))

            // Transaction list
            if (transactions.isEmpty()) {
                Text(
                    text = "No transactions yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    transactions.forEach { transaction ->
                        RecentTransactionItem(
                            transaction = transaction,
                            categories = categories,
                            onClick = { onTransactionClick(transaction) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Single transaction item in the recent transactions list
 */
@Composable
private fun RecentTransactionItem(
    transaction: RecentTransaction,
    categories: List<Category>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = SimpleDateFormat("MMM dd", Locale.getDefault())
    val categoryMap = categories.associateBy { it.id }

    when (transaction) {
        is RecentTransaction.ExpenseTransaction -> {
            val expense = transaction.expense
            val category = expense.categoryId?.let { categoryMap[it] }
            val formattedDate = dateFormatter.format(Date(expense.date))

            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category icon
                Icon(
                    imageVector = IconMapper.getIcon(category?.icon ?: "category"),
                    contentDescription = category?.name ?: "Uncategorized",
                    tint = if (category != null) Color(category.color) else Color.Gray,
                    modifier = Modifier.padding(end = 12.dp)
                )

                // Description and date
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = expense.description,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "$formattedDate • ${expense.paymentMethod.toDisplayName()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Amount with - symbol in red
                Text(
                    text = "- ${CurrencyUtils.formatPaise(expense.amount)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFC62828) // Red for expense
                )
            }
        }

        is RecentTransaction.IncomeTransaction -> {
            val income = transaction.income
            val category = income.categoryId?.let { categoryMap[it] }
            val formattedDate = dateFormatter.format(Date(income.date))

            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category icon (or generic income icon if no category)
                Icon(
                    imageVector = IconMapper.getIcon(category?.icon ?: "attach_money"),
                    contentDescription = category?.name ?: "Income",
                    tint = if (category != null) Color(category.color) else Color(0xFF2E7D32),
                    modifier = Modifier.padding(end = 12.dp)
                )

                // Description and date
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = income.description ?: "Income",
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "$formattedDate • ${category?.name ?: income.source.toDisplayString()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Amount with + symbol in green
                Text(
                    text = "+ ${CurrencyUtils.formatPaise(income.amount)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF2E7D32) // Green for income
                )
            }
        }
    }
}
