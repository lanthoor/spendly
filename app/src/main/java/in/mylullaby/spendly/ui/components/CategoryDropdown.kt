package `in`.mylullaby.spendly.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.CaretDown
import `in`.mylullaby.spendly.domain.model.Category
import kotlinx.coroutines.flow.collectLatest

/**
 * Dropdown menu for selecting a category.
 * Displays category icon and name for each option.
 *
 * @param selectedCategory Currently selected category (null for Uncategorized)
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
    label: String = "Category",
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var showDialog by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collectLatest { interaction ->
            if (interaction is PressInteraction.Release) {
                showDialog = true
            }
        }
    }

    OutlinedTextField(
        value = selectedCategory?.name ?: "Misc",
        onValueChange = { /* Read-only */ },
        label = { Text(label) },
        leadingIcon = selectedCategory?.let { category ->
            {
                Icon(
                    imageVector = IconMapper.getIcon(category.icon),
                    contentDescription = category.name,
                    tint = Color(category.color)
                )
            }
        },
        trailingIcon = {
            Icon(
                imageVector = PhosphorIcons.Regular.CaretDown,
                contentDescription = "Select category"
            )
        },
        readOnly = true,
        interactionSource = interactionSource,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        singleLine = true
    )

    if (showDialog) {
        CategorySelectionDialog(
            selectedCategory = selectedCategory,
            categories = categories,
            onCategorySelected = onCategorySelected,
            onDismiss = { showDialog = false }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CategoryDropdownPreview() {
    val sampleCategories = listOf(
        Category(1, "Food & Dining", "restaurant", 0xFFFF6B6B.toInt(), false, 1),
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
