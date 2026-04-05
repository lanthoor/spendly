package dev.lanthoor.spendly.ui.screens.transactions.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import dev.lanthoor.spendly.core.model.finance.RecentTransaction
import dev.lanthoor.spendly.domain.model.Account
import dev.lanthoor.spendly.domain.model.Category
import dev.lanthoor.spendly.ui.components.IconMapper
import dev.lanthoor.spendly.ui.theme.adjustForTheme
import dev.lanthoor.spendly.ui.theme.expenseColor
import dev.lanthoor.spendly.ui.theme.incomeColor
import dev.lanthoor.spendly.ui.theme.isDark
import dev.lanthoor.spendly.utils.CurrencyUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionListItem(
    transaction: RecentTransaction,
    categories: List<Category>,
    accounts: List<Account>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
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
                    .clickable(onClick = onClick)
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isDark = MaterialTheme.colorScheme.isDark
                Icon(
                    imageVector = IconMapper.getIcon(category?.icon ?: "category"),
                    contentDescription = category?.name ?: stringResource(R.string.label_others),
                    tint = if (category != null) {
                        Color(category.color).adjustForTheme(isDark)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.padding(end = 12.dp)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = expense.description,
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (expense.smsBody != null) {
                            Icon(
                                imageVector = PhosphorIcons.Regular.ChatText,
                                contentDescription = stringResource(R.string.cd_created_from_sms),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text(
                        text = "$formattedDate • ${account?.name ?: stringResource(R.string.label_unknown)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "- ${CurrencyUtils.formatPaise(expense.amount)}",
                    style = MaterialTheme.typography.bodyLarge,
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
                    .clickable(onClick = onClick)
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isDarkIncome = MaterialTheme.colorScheme.isDark
                Icon(
                    imageVector = IconMapper.getIcon(category?.icon ?: "attach_money"),
                    contentDescription = category?.name ?: stringResource(R.string.label_income),
                    tint = if (category != null) {
                        Color(category.color).adjustForTheme(isDarkIncome)
                    } else {
                        MaterialTheme.colorScheme.incomeColor()
                    },
                    modifier = Modifier.padding(end = 12.dp)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = income.description,
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (income.smsBody != null) {
                            Icon(
                                imageVector = PhosphorIcons.Regular.ChatText,
                                contentDescription = stringResource(R.string.cd_created_from_sms),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text(
                        text = "$formattedDate • ${account?.name ?: stringResource(R.string.label_unknown)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "+ ${CurrencyUtils.formatPaise(income.amount)}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.incomeColor()
                )
            }
        }
    }
}
