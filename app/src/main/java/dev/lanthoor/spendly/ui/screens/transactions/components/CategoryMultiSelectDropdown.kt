package dev.lanthoor.spendly.ui.screens.transactions.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.CaretDown
import com.adamglin.phosphoricons.regular.Folders
import dev.lanthoor.spendly.R
import dev.lanthoor.spendly.domain.model.Category
import dev.lanthoor.spendly.ui.components.IconMapper
import dev.lanthoor.spendly.ui.theme.adjustForTheme
import dev.lanthoor.spendly.ui.theme.isDark

/**
 * Multi-select dropdown for categories with button style matching type dropdown.
 */
@Composable
fun CategoryFilterChip(
    selectedCategories: Set<Long>,
    allCategories: List<Category>,
    onCategoryToggle: (Long) -> Unit,
    onClearSelection: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    // Button label
    val buttonLabel = when {
        selectedCategories.isEmpty() -> stringResource(R.string.label_all)
        selectedCategories.size == 1 -> {
            allCategories.find { it.id in selectedCategories }?.name
                ?: pluralStringResource(R.plurals.plurals_selected_count, 1, 1)
        }

        else -> pluralStringResource(
            R.plurals.plurals_selected_count,
            selectedCategories.size,
            selectedCategories.size
        )
    }

    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { expanded = true }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = PhosphorIcons.Regular.Folders,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = buttonLabel,
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
            Column(
                modifier = Modifier
                    .width(320.dp)
                    .height(400.dp)
                    .padding(16.dp)
            ) {
                // Header with clear button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.title_select_categories),
                        style = MaterialTheme.typography.titleSmall
                    )
                    if (selectedCategories.isNotEmpty()) {
                        TextButton(
                            onClick = {
                                onClearSelection()
                            }
                        ) {
                            Text(stringResource(R.string.button_clear))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                // Category list with fixed height
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                ) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(allCategories) { category ->
                            val isSelected = category.id in selectedCategories
                            val isDark = MaterialTheme.colorScheme.isDark

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onCategoryToggle(category.id) }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = null
                                )
                                Icon(
                                    imageVector = IconMapper.getIcon(category.icon),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .padding(start = 8.dp)
                                        .size(20.dp),
                                    tint = Color(category.color).adjustForTheme(isDark)
                                )
                                Text(
                                    text = category.name,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }

                // Action buttons
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { expanded = false }) {
                        Text(stringResource(R.string.button_cancel))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = { expanded = false }) {
                        Text(stringResource(R.string.button_apply))
                    }
                }
            }
        }
    }
}
