package dev.lanthoor.spendly.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.CaretDown
import dev.lanthoor.spendly.R
import dev.lanthoor.spendly.domain.model.Category
import dev.lanthoor.spendly.ui.theme.adjustForTheme
import dev.lanthoor.spendly.ui.theme.isDark

/**
 * Dropdown menu for selecting a category.
 * Displays category icon and name for each option in a scrollable list.
 *
 * @param selectedCategory Currently selected category (null for Others)
 * @param categories List of available categories
 * @param onCategorySelected Callback when a category is selected
 * @param label Label for the dropdown field
 * @param modifier Optional modifier
 * @param enabled Whether the field is enabled
 */
@Composable
fun CategoryDropdown(
    selectedCategory: Category?,
    categories: List<Category>,
    onCategorySelected: (Category?) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    enabled: Boolean = true
) {
    val displayLabel = label ?: stringResource(R.string.label_category)
    var expanded by remember { mutableStateOf(false) }
    var buttonWidth by remember { mutableIntStateOf(0) }
    val isDark = MaterialTheme.colorScheme.isDark
    val density = LocalDensity.current

    Box(modifier = modifier) {
        Column(
            modifier = Modifier.onSizeChanged { buttonWidth = it.width }
        ) {
            // Label text
            Text(
                text = displayLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
            )

            // Button trigger
            OutlinedButton(
                onClick = { if (enabled) expanded = true },
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                contentPadding = PaddingValues(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Leading icon (if category selected)
                    selectedCategory?.let { category ->
                        Icon(
                            imageVector = IconMapper.getIcon(category.icon),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = Color(category.color).adjustForTheme(isDark)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    // Text
                    Text(
                        text = selectedCategory?.name ?: stringResource(R.string.label_others),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Start
                    )

                    // Trailing caret
                    Icon(
                        imageVector = PhosphorIcons.Regular.CaretDown,
                        contentDescription = stringResource(R.string.cd_select_category),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Dropdown menu
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = with(density) { Modifier.width(buttonWidth.toDp()) }
        ) {
            Column(
                modifier = Modifier
                    .heightIn(max = 360.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Category items
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category.name) },
                        onClick = {
                            onCategorySelected(category)
                            expanded = false
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = IconMapper.getIcon(category.icon),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = Color(category.color).adjustForTheme(isDark)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (selectedCategory?.id == category.id) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    Color.Transparent
                                }
                            )
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CategoryDropdownPreview() {
    val sampleCategories = listOf(
        Category(1, "Food", "restaurant", 0xFFFF6B6B.toInt(), false, 1),
        Category(2, "Travel", "flight", 0xFF4ECDC4.toInt(), false, 2),
        Category(3, "Shopping", "shopping_cart", 0xFFFF9FF3.toInt(), false, 3)
    )

    CategoryDropdown(
        selectedCategory = sampleCategories[0],
        categories = sampleCategories,
        onCategorySelected = {},
        modifier = Modifier.padding(16.dp)
    )
}
