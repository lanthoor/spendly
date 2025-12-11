package `in`.mylullaby.spendly.ui.screens.accounts.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.*
import `in`.mylullaby.spendly.ui.components.IconMapper
import `in`.mylullaby.spendly.ui.screens.accounts.AccountFormField
import `in`.mylullaby.spendly.ui.screens.accounts.AccountFormState
import `in`.mylullaby.spendly.utils.AccountType
import `in`.mylullaby.spendly.utils.getDefaultIcon
import `in`.mylullaby.spendly.utils.toDisplayName
import kotlinx.coroutines.flow.collectLatest

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
    var showTypeDialog by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Account Name
        OutlinedTextField(
            value = formState.name,
            onValueChange = { onFieldChange(AccountFormField.NAME, it) },
            label = { Text("Account Name") },
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
        val typeInteractionSource = remember { MutableInteractionSource() }

        LaunchedEffect(typeInteractionSource) {
            typeInteractionSource.interactions.collectLatest { interaction ->
                if (interaction is PressInteraction.Release && enabled) {
                    showTypeDialog = true
                }
            }
        }

        OutlinedTextField(
            value = formState.type.toDisplayName(),
            onValueChange = { },
            label = { Text("Account Type") },
            leadingIcon = {
                Icon(
                    imageVector = IconMapper.getIcon(formState.type.getDefaultIcon()),
                    contentDescription = null
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = PhosphorIcons.Regular.CaretDown,
                    contentDescription = "Select type"
                )
            },
            readOnly = true,
            interactionSource = typeInteractionSource,
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
                    text = "Icon Preview",
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
                    text = "Color",
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
                        contentDescription = "Pick color",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pick Color", color = Color.White)
                }
            }
        }
    }

    // Account Type Selection Dialog
    if (showTypeDialog) {
        AccountTypeSelectionDialog(
            selectedType = formState.type,
            onTypeSelected = { type ->
                onFieldChange(AccountFormField.TYPE, type)
                onFieldChange(AccountFormField.ICON, type.getDefaultIcon())
                showTypeDialog = false
            },
            onDismiss = { showTypeDialog = false }
        )
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
        title = { Text("Select Color") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                colors.chunked(4).forEach { rowColors ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
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
                Text("Cancel")
            }
        }
    )
}
