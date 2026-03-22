package dev.lanthoor.spendly.ui.screens.transactions.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowsDownUp
import com.adamglin.phosphoricons.regular.CaretDown
import com.adamglin.phosphoricons.regular.TrendDown
import com.adamglin.phosphoricons.regular.TrendUp
import dev.lanthoor.spendly.R
import dev.lanthoor.spendly.ui.screens.transactions.TransactionType
import dev.lanthoor.spendly.ui.theme.expenseColor
import dev.lanthoor.spendly.ui.theme.incomeColor

/**
 * Dropdown for selecting transaction type with icons.
 */
@Composable
fun TransactionTypeDropdown(
    selectedType: TransactionType,
    onTypeSelected: (TransactionType) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { expanded = true }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (selectedType) {
                        TransactionType.ALL -> PhosphorIcons.Regular.ArrowsDownUp
                        TransactionType.EXPENSE -> PhosphorIcons.Regular.TrendDown
                        TransactionType.INCOME -> PhosphorIcons.Regular.TrendUp
                    },
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = when (selectedType) {
                        TransactionType.ALL -> MaterialTheme.colorScheme.onSurface
                        TransactionType.EXPENSE -> MaterialTheme.colorScheme.expenseColor()
                        TransactionType.INCOME -> MaterialTheme.colorScheme.incomeColor()
                    }
                )
                Text(
                    text = when (selectedType) {
                        TransactionType.ALL -> stringResource(R.string.label_all)
                        TransactionType.EXPENSE -> stringResource(R.string.label_expense)
                        TransactionType.INCOME -> stringResource(R.string.label_income)
                    },
                    modifier = Modifier.padding(start = 4.dp),
                    style = MaterialTheme.typography.bodySmall
                )
                Icon(
                    imageVector = PhosphorIcons.Regular.CaretDown,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            TransactionType.entries.forEach { type ->
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when (type) {
                                    TransactionType.ALL -> PhosphorIcons.Regular.ArrowsDownUp
                                    TransactionType.EXPENSE -> PhosphorIcons.Regular.TrendDown
                                    TransactionType.INCOME -> PhosphorIcons.Regular.TrendUp
                                },
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = when (type) {
                                    TransactionType.ALL -> MaterialTheme.colorScheme.onSurface
                                    TransactionType.EXPENSE -> MaterialTheme.colorScheme.expenseColor()
                                    TransactionType.INCOME -> MaterialTheme.colorScheme.incomeColor()
                                }
                            )
                            Text(
                                text = when (type) {
                                    TransactionType.ALL -> stringResource(R.string.screen_transactions_title)
                                    TransactionType.EXPENSE -> stringResource(R.string.label_expenses)
                                    TransactionType.INCOME -> stringResource(R.string.label_income)
                                },
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    },
                    onClick = {
                        onTypeSelected(type)
                        expanded = false
                    }
                )
            }
        }
    }
}
