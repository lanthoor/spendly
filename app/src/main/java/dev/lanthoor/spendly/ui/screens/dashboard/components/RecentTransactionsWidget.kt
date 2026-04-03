package dev.lanthoor.spendly.ui.screens.dashboard.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ChatText
import dev.lanthoor.spendly.R
import dev.lanthoor.spendly.domain.model.Account
import dev.lanthoor.spendly.domain.model.Category
import dev.lanthoor.spendly.ui.components.IconMapper
import dev.lanthoor.spendly.ui.screens.dashboard.RecentTransaction
import dev.lanthoor.spendly.ui.theme.adjustForTheme
import dev.lanthoor.spendly.ui.theme.expenseColor
import dev.lanthoor.spendly.ui.theme.incomeColor
import dev.lanthoor.spendly.ui.theme.isDark
import dev.lanthoor.spendly.utils.CurrencyUtils
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
    accounts: List<Account>,
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
                text = stringResource(R.string.label_recent_transactions),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

            // Transaction list
            if (transactions.isEmpty()) {
                Text(
                    text = stringResource(R.string.empty_no_transactions_desc),
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
                            accounts = accounts,
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
    accounts: List<Account>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = SimpleDateFormat("MMM dd", Locale.getDefault())
    val categoryMap = categories.associateBy { it.id }
    val accountMap = accounts.associateBy { it.id }

    when (transaction) {
        is RecentTransaction.ExpenseTransaction -> {
            val expense = transaction.expense
            val category = expense.categoryId?.let { categoryMap[it] }
            val account = accountMap[expense.accountId]
            val formattedDate = dateFormatter.format(Date(expense.date))

            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category icon
                val isDark = MaterialTheme.colorScheme.isDark
                Icon(
                    imageVector = IconMapper.getIcon(category?.icon ?: "category"),
                    contentDescription = category?.name ?: stringResource(R.string.label_others),
                    tint = if (category != null) Color(category.color).adjustForTheme(isDark) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 12.dp)
                )

                // Description and date
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = expense.description,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        // SMS envelope icon if transaction was created from SMS
                        if (expense.smsBody != null) {
                            Icon(
                                imageVector = PhosphorIcons.Regular.ChatText,
                                contentDescription = stringResource(R.string.cd_created_from_sms),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Text(
                        text = "$formattedDate • ${account?.name ?: stringResource(R.string.label_unknown)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Amount with - symbol in red
                Text(
                    text = "- ${CurrencyUtils.formatPaise(expense.amount)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.expenseColor()
                )
            }
        }

        is RecentTransaction.IncomeTransaction -> {
            val income = transaction.income
            val category = income.categoryId?.let { categoryMap[it] }
            val account = accountMap[income.accountId]
            val formattedDate = dateFormatter.format(Date(income.date))

            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category icon (or generic income icon if no category)
                val isDarkIncome = MaterialTheme.colorScheme.isDark
                Icon(
                    imageVector = IconMapper.getIcon(category?.icon ?: "attach_money"),
                    contentDescription = category?.name ?: stringResource(R.string.label_income),
                    tint = if (category != null) Color(category.color).adjustForTheme(isDarkIncome) else MaterialTheme.colorScheme.incomeColor(),
                    modifier = Modifier.padding(end = 12.dp)
                )

                // Description and date
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = income.description,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        // SMS envelope icon if transaction was created from SMS
                        if (income.smsBody != null) {
                            Icon(
                                imageVector = PhosphorIcons.Regular.ChatText,
                                contentDescription = stringResource(R.string.cd_created_from_sms),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Text(
                        text = "$formattedDate • ${account?.name ?: stringResource(R.string.label_unknown)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Amount with + symbol in green
                Text(
                    text = "+ ${CurrencyUtils.formatPaise(income.amount)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.incomeColor()
                )
            }
        }
    }
}
