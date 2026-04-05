package dev.lanthoor.spendly.ui.screens.accounts.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Palette
import com.adamglin.phosphoricons.regular.Textbox
import dev.lanthoor.spendly.R
import dev.lanthoor.spendly.ui.components.AccountTypeDropdown
import dev.lanthoor.spendly.ui.components.IconMapper
import dev.lanthoor.spendly.ui.screens.accounts.AccountFormField
import dev.lanthoor.spendly.ui.screens.accounts.AccountFormState
import dev.lanthoor.spendly.core.ui.format.getDefaultIcon

/**
 * Reusable form fields component for add/edit account screens.
 */
@Composable
fun AccountFormFields(
    formState: AccountFormState,
    onFieldChange: (AccountFormField, Any) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var showColorPicker by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Account Name
        OutlinedTextField(
            value = formState.name,
            onValueChange = { onFieldChange(AccountFormField.NAME, it) },
            label = { Text(stringResource(R.string.label_account)) },
            leadingIcon = {
                Icon(
                    imageVector = PhosphorIcons.Regular.Textbox,
                    contentDescription = null
                )
            },
            isError = formState.errors.containsKey(AccountFormField.NAME),
            supportingText = formState.errors[AccountFormField.NAME]?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            singleLine = true
        )

        // Account Type
        AccountTypeDropdown(
            selectedType = formState.type,
            onTypeSelected = { type ->
                onFieldChange(AccountFormField.TYPE, type)
                onFieldChange(AccountFormField.ICON, type.getDefaultIcon())
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled
        )

        // Icon & Color Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Preview
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.label_icon_preview),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = IconMapper.getIcon(formState.icon),
                        contentDescription = null,
                        tint = Color(formState.color),
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            // Color Picker
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.label_color),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = { if (enabled) showColorPicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(formState.color)
                    )
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.Palette,
                        contentDescription = stringResource(R.string.label_pick_color),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.label_pick_color), color = Color.White)
                }
            }
        }
    }

    // Color Picker Dialog
    if (showColorPicker) {
        ColorPickerDialog(
            selectedColor = formState.color,
            onColorSelected = { color ->
                onFieldChange(AccountFormField.COLOR, color)
                showColorPicker = false
            },
            onDismiss = { showColorPicker = false }
        )
    }
}

/**
 * Color picker dialog with predefined colors
 */
@Composable
private fun ColorPickerDialog(
    selectedColor: Int,
    onColorSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = listOf(
        0xFF00BFA5, // Teal
        0xFF1E88E5, // Blue
        0xFF43A047, // Green
        0xFFFDD835, // Yellow
        0xFFFF6F00, // Orange
        0xFFE53935, // Red
        0xFF8E24AA, // Purple
        0xFFD81B60, // Pink
        0xFF546E7A, // Blue Grey
        0xFF6D4C41, // Brown
        0xFF757575, // Grey
        0xFF212121  // Dark Grey
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.title_select_color)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                colors.chunked(4).forEach { rowColors ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(
                            16.dp,
                            Alignment.CenterHorizontally
                        )
                    ) {
                        rowColors.forEach { color ->
                            val isSelected = color.toInt() == selectedColor
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(Color(color.toInt()))
                                    .border(
                                        width = if (isSelected) 3.dp else 0.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { onColorSelected(color.toInt()) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.button_cancel))
            }
        }
    )
}
