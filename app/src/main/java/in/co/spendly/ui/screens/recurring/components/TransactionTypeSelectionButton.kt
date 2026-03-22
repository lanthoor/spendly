package `in`.co.spendly.ui.screens.recurring.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowCircleDown
import com.adamglin.phosphoricons.regular.ArrowCircleUp
import `in`.co.spendly.utils.TransactionType

/**
 * Segmented button for selecting transaction type (Expense/Income).
 *
 * @param selectedType Currently selected transaction type
 * @param onTypeSelected Callback when a type is selected
 * @param modifier Modifier for the component
 */
@Composable
fun TransactionTypeSelectionButton(
    selectedType: TransactionType,
    onTypeSelected: (TransactionType) -> Unit,
    modifier: Modifier = Modifier
) {
    val types = TransactionType.entries

    SingleChoiceSegmentedButtonRow(
        modifier = modifier.fillMaxWidth()
    ) {
        types.forEachIndexed { index, type ->
            SegmentedButton(
                selected = selectedType == type,
                onClick = { onTypeSelected(type) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = types.size
                ),
                icon = {
                    SegmentedButtonDefaults.Icon(active = selectedType == type) {
                        Icon(
                            imageVector = when (type) {
                                TransactionType.EXPENSE -> PhosphorIcons.Regular.ArrowCircleDown
                                TransactionType.INCOME -> PhosphorIcons.Regular.ArrowCircleUp
                            },
                            contentDescription = type.name
                        )
                    }
                },
                label = {
                    Text(
                        text = when (type) {
                            TransactionType.EXPENSE -> "Expense"
                            TransactionType.INCOME -> "Income"
                        },
                        fontWeight = if (selectedType == type) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            )
        }
    }
}
