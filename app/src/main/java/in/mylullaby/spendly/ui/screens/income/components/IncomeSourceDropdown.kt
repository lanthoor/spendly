package `in`.mylullaby.spendly.ui.screens.income.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.vector.ImageVector
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowsClockwise
import com.adamglin.phosphoricons.regular.Briefcase
import com.adamglin.phosphoricons.regular.CaretDown
import com.adamglin.phosphoricons.regular.ChartLine
import com.adamglin.phosphoricons.regular.CreditCard
import com.adamglin.phosphoricons.regular.Gift
import com.adamglin.phosphoricons.regular.House
import com.adamglin.phosphoricons.regular.Money
import com.adamglin.phosphoricons.regular.Percent
import `in`.mylullaby.spendly.utils.IncomeSource
import kotlinx.coroutines.flow.collectLatest

/**
 * Dropdown menu for selecting an income source.
 * Displays friendly names for each income source enum value.
 *
 * @param selectedSource Currently selected income source
 * @param onSourceSelected Callback when an income source is selected
 * @param label Label for the dropdown field
 * @param modifier Optional modifier
 * @param enabled Whether the field is enabled
 */
@Composable
fun IncomeSourceDropdown(
    selectedSource: IncomeSource,
    onSourceSelected: (IncomeSource) -> Unit,
    label: String = "Income Source",
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
        value = selectedSource.toDisplayString(),
        onValueChange = { /* Read-only */ },
        label = { Text(label) },
        leadingIcon = {
            Icon(
                imageVector = selectedSource.getIcon(),
                contentDescription = selectedSource.toDisplayString()
            )
        },
        trailingIcon = {
            Icon(
                imageVector = PhosphorIcons.Regular.CaretDown,
                contentDescription = "Select income source"
            )
        },
        readOnly = true,
        interactionSource = interactionSource,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        singleLine = true
    )

    if (showDialog) {
        IncomeSourceSelectionDialog(
            selectedSource = selectedSource,
            onSourceSelected = onSourceSelected,
            onDismiss = { showDialog = false }
        )
    }
}

/**
 * Get icon for income source
 */
fun IncomeSource.getIcon(): ImageVector {
    return when (this) {
        IncomeSource.SALARY -> PhosphorIcons.Regular.Money
        IncomeSource.FREELANCE -> PhosphorIcons.Regular.Briefcase
        IncomeSource.INVESTMENT -> PhosphorIcons.Regular.ChartLine
        IncomeSource.GIFTS -> PhosphorIcons.Regular.Gift
        IncomeSource.REFUND -> PhosphorIcons.Regular.ArrowsClockwise
        IncomeSource.BUSINESS -> PhosphorIcons.Regular.Briefcase
        IncomeSource.RENTAL -> PhosphorIcons.Regular.House
        IncomeSource.INTEREST -> PhosphorIcons.Regular.Percent
        IncomeSource.OTHER -> PhosphorIcons.Regular.CreditCard
    }
}

/**
 * Convert income source to display string
 */
fun IncomeSource.toDisplayString(): String {
    return when (this) {
        IncomeSource.SALARY -> "Salary"
        IncomeSource.FREELANCE -> "Freelance"
        IncomeSource.INVESTMENT -> "Investment"
        IncomeSource.GIFTS -> "Gifts"
        IncomeSource.REFUND -> "Refund"
        IncomeSource.BUSINESS -> "Business"
        IncomeSource.RENTAL -> "Rental"
        IncomeSource.INTEREST -> "Interest"
        IncomeSource.OTHER -> "Other"
    }
}
