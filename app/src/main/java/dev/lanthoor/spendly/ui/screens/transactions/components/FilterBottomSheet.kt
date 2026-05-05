package dev.lanthoor.spendly.ui.screens.transactions.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowsDownUp
import com.adamglin.phosphoricons.regular.CalendarBlank
import com.adamglin.phosphoricons.regular.TrendDown
import com.adamglin.phosphoricons.regular.TrendUp
import dev.lanthoor.spendly.R
import dev.lanthoor.spendly.core.model.finance.RecentTransaction
import dev.lanthoor.spendly.domain.model.Category
import dev.lanthoor.spendly.ui.components.IconMapper
import dev.lanthoor.spendly.ui.screens.transactions.TransactionType
import dev.lanthoor.spendly.ui.theme.adjustForTheme
import dev.lanthoor.spendly.ui.theme.isDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Bottom sheet for filtering transactions.
 * Contains date range, transaction type, and category filters.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    startDate: Long?,
    endDate: Long?,
    selectedType: TransactionType,
    selectedCategories: Set<Long>,
    allCategories: List<Category>,
    allTransactions: List<RecentTransaction>,
    onApplyFilters: (Long?, Long?, TransactionType, Set<Long>) -> Unit,
    onClearFilters: () -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState,
    modifier: Modifier = Modifier
) {
    // Local state for filter changes
    var tempType by remember(selectedType) { mutableStateOf(selectedType) }
    var tempCategories by remember(selectedCategories) { mutableStateOf(selectedCategories) }

    // Date picker dialog state
    var showDatePickerDialog by remember { mutableStateOf(false) }

    // Date picker state
    val dateRangePickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = startDate,
        initialSelectedEndDateMillis = endDate
    )

    // Format date range for display
    val dateFormatter = remember { SimpleDateFormat("MMM dd", Locale.getDefault()) }
    val dateRangeLabel = when {
        dateRangePickerState.selectedStartDateMillis != null &&
                dateRangePickerState.selectedEndDateMillis != null -> {
            val start = dateFormatter.format(Date(dateRangePickerState.selectedStartDateMillis!!))
            val end = dateFormatter.format(Date(dateRangePickerState.selectedEndDateMillis!!))
            "$start - $end"
        }

        else -> stringResource(R.string.label_all)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
        ) {
            // Title
            Text(
                text = stringResource(R.string.screen_filter_transactions_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Date Range Section
            Text(
                text = stringResource(R.string.label_date_range),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            FilterChip(
                selected = dateRangePickerState.selectedStartDateMillis != null,
                onClick = { showDatePickerDialog = true },
                label = { Text(dateRangeLabel) },
                leadingIcon = {
                    Icon(
                        imageVector = PhosphorIcons.Regular.CalendarBlank,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Transaction Type Section
            Text(
                text = stringResource(R.string.label_transaction_type),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                TransactionType.entries.forEachIndexed { index, type ->
                    SegmentedButton(
                        selected = tempType == type,
                        onClick = { tempType = type },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = TransactionType.entries.size
                        ),
                        icon = {
                            Icon(
                                imageVector = when (type) {
                                    TransactionType.ALL -> PhosphorIcons.Regular.ArrowsDownUp
                                    TransactionType.EXPENSE -> PhosphorIcons.Regular.TrendDown
                                    TransactionType.INCOME -> PhosphorIcons.Regular.TrendUp
                                },
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    ) {
                        Text(
                            text = when (type) {
                                TransactionType.ALL -> stringResource(R.string.label_all)
                                TransactionType.EXPENSE -> stringResource(R.string.label_expense)
                                TransactionType.INCOME -> stringResource(R.string.label_income)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Categories Section
            Text(
                text = stringResource(R.string.label_categories),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Filter categories based on selected type and date range
            val filteredCategories = run {
                // First, filter transactions by date range
                val pickerStartDate = dateRangePickerState.selectedStartDateMillis
                val pickerEndDate = dateRangePickerState.selectedEndDateMillis
                val transactionsInRange = if (pickerStartDate != null && pickerEndDate != null) {
                    allTransactions.filter { transaction ->
                        val date = when (transaction) {
                            is RecentTransaction.ExpenseTransaction -> transaction.expense.date
                            is RecentTransaction.IncomeTransaction -> transaction.income.date
                        }
                        date in pickerStartDate..pickerEndDate
                    }
                } else {
                    allTransactions
                }

                // Get category IDs used in those transactions
                val usedCategoryIds = transactionsInRange.mapNotNull { transaction ->
                    when (transaction) {
                        is RecentTransaction.ExpenseTransaction -> transaction.expense.categoryId
                        is RecentTransaction.IncomeTransaction -> transaction.income.categoryId
                    }
                }.toSet()

                // Filter categories by usage (all categories now work for both expense and income)
                allCategories.filter { category ->
                    category.id in usedCategoryIds
                }
            }

            // Horizontal carousel of category toggle buttons
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredCategories) { category ->
                    val isSelected = category.id in tempCategories
                    val isDark = MaterialTheme.colorScheme.isDark

                    Card(
                        onClick = {
                            tempCategories = if (isSelected) {
                                tempCategories - category.id
                            } else {
                                tempCategories + category.id
                            }
                        },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                        modifier = Modifier.width(100.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = IconMapper.getIcon(category.icon),
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = Color(category.color).adjustForTheme(isDark)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = category.name,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 2,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        onClearFilters()
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.button_clear_all))
                }
                Button(
                    onClick = {
                        // Get values directly from picker state
                        val finalStartDate = dateRangePickerState.selectedStartDateMillis
                        val finalEndDate = dateRangePickerState.selectedEndDateMillis
                        onApplyFilters(finalStartDate, finalEndDate, tempType, tempCategories)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.button_apply))
                }
            }
        }
    }

    // Date Range Picker Dialog
    if (showDatePickerDialog) {
        AlertDialog(
            onDismissRequest = { showDatePickerDialog = false },
            title = { Text(stringResource(R.string.title_select_date_range)) },
            text = {
                Column {
                    DateRangePicker(
                        state = dateRangePickerState,
                        title = null,
                        headline = null,
                        showModeToggle = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(450.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showDatePickerDialog = false }
                ) {
                    Text(stringResource(R.string.button_ok))
                }
            },
            dismissButton = {
                Row {
                    if (dateRangePickerState.selectedStartDateMillis != null ||
                        dateRangePickerState.selectedEndDateMillis != null
                    ) {
                        TextButton(
                            onClick = {
                                dateRangePickerState.setSelection(null, null)
                            }
                        ) {
                            Text(stringResource(R.string.button_clear))
                        }
                    }
                    TextButton(onClick = { showDatePickerDialog = false }) {
                        Text(stringResource(R.string.button_cancel))
                    }
                }
            }
        )
    }
}
