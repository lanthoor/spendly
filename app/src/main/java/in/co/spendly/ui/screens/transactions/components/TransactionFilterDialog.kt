package `in`.co.spendly.ui.screens.transactions.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.co.spendly.R
import `in`.co.spendly.domain.model.Category
import `in`.co.spendly.ui.components.DateRangePickerModal
import `in`.co.spendly.ui.screens.transactions.TransactionType
import `in`.co.spendly.ui.theme.isDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Dialog for filtering transactions by date range, type, and category
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TransactionFilterDialog(
    startDate: Long?,
    endDate: Long?,
    selectedType: TransactionType,
    selectedCategories: Set<Long>,
    allCategories: List<Category>,
    onDateRangeChange: (Long?, Long?) -> Unit,
    onTypeChange: (TransactionType) -> Unit,
    onCategoryToggle: (Long) -> Unit,
    onClearFilters: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var tempStartDate by remember(startDate) { mutableLongStateOf(startDate ?: 0L) }
    var tempEndDate by remember(endDate) { mutableLongStateOf(endDate ?: 0L) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.screen_filter_transactions_title)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Date Range Section
                Column {
                    Text(
                        text = stringResource(R.string.label_date_range),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = { showDatePicker = true }
                    ) {
                        Text(
                            text = if (startDate != null && endDate != null) {
                                val formatter =
                                    SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                                "${formatter.format(Date(startDate))} - ${
                                    formatter.format(
                                        Date(
                                            endDate
                                        )
                                    )
                                }"
                            } else {
                                stringResource(R.string.label_select_date_range)
                            }
                        )
                    }
                    if (startDate != null || endDate != null) {
                        TextButton(onClick = { onDateRangeChange(null, null) }) {
                            Text(stringResource(R.string.button_clear_date_range))
                        }
                    }
                }

                HorizontalDivider()

                // Transaction Type Section
                Column {
                    Text(
                        text = stringResource(R.string.label_transaction_type),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TransactionType.entries.forEach { type ->
                            FilterChip(
                                selected = selectedType == type,
                                onClick = { onTypeChange(type) },
                                label = {
                                    Text(
                                        type.name.lowercase().replaceFirstChar { it.uppercase() })
                                }
                            )
                        }
                    }
                }

                HorizontalDivider()

                // Category Section
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.label_categories),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (selectedCategories.isNotEmpty()) {
                            TextButton(onClick = { selectedCategories.forEach { onCategoryToggle(it) } }) {
                                Text(stringResource(R.string.button_clear))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    // All categories now work for both expense and income
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        allCategories.forEach { category ->
                            MaterialTheme.colorScheme.isDark
                            FilterChip(
                                selected = category.id in selectedCategories,
                                onClick = { onCategoryToggle(category.id) },
                                label = { Text(category.name) },
                                leadingIcon = null
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.button_apply))
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onClearFilters()
                onDismiss()
            }) {
                Text(stringResource(R.string.button_clear_all))
            }
        },
        modifier = modifier
    )

    // Date Range Picker
    if (showDatePicker) {
        DateRangePickerModal(
            onDateRangeSelected = { start, end ->
                tempStartDate = start ?: 0L
                tempEndDate = end ?: 0L
                onDateRangeChange(start, end)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}
