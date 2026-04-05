package dev.lanthoor.spendly.ui.screens.analytics.components.canvas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import dev.lanthoor.spendly.domain.model.LineChartEntry
import dev.lanthoor.spendly.utils.CurrencyUtils

@Composable
internal fun LineChartTooltip(
    point: LineChartEntry,
    pointIndex: Int,
    pointPosition: androidx.compose.ui.geometry.Offset,
    incomeData: List<LineChartEntry>,
    expenseData: List<LineChartEntry>,
    netWorthData: List<LineChartEntry>,
    incomeColor: Color,
    expenseColor: Color,
    netWorthColor: Color,
    incomeLabel: String,
    expenseLabel: String,
    netWorthLabel: String
) {
    val sameIndexIncome = if (pointIndex < incomeData.size) incomeData[pointIndex] else null
    val sameIndexExpense = if (pointIndex < expenseData.size) expenseData[pointIndex] else null
    val sameIndexNetWorth = if (pointIndex < netWorthData.size) netWorthData[pointIndex] else null

    val popoverWidth = 200.dp
    val popoverHeight = 120.dp

    val density = androidx.compose.ui.platform.LocalDensity.current
    val shouldShowAbove = with(density) { pointPosition.y > popoverHeight.toPx() }

    val horizontalOffset = with(density) {
        (pointPosition.x - (popoverWidth.toPx() / 2f)).coerceIn(
            16.dp.toPx(),
            280.dp.toPx() - popoverWidth.toPx() - 16.dp.toPx()
        )
    }

    Box(
        modifier = Modifier
            .offset {
                with(density) {
                    IntOffset(
                        x = horizontalOffset.toInt(),
                        y = if (shouldShowAbove) {
                            (pointPosition.y - popoverHeight.toPx() - 16.dp.toPx()).toInt()
                        } else {
                            (pointPosition.y + 24.dp.toPx()).toInt()
                        }
                    )
                }
            }
            .width(popoverWidth)
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = point.dateLabel,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.size(8.dp))

                if (sameIndexIncome != null && sameIndexIncome.amount > 0) {
                    ValueRow(
                        color = incomeColor,
                        label = incomeLabel,
                        amount = sameIndexIncome.amount
                    )
                }

                if (sameIndexExpense != null && sameIndexExpense.amount > 0) {
                    if (sameIndexIncome != null && sameIndexIncome.amount > 0) {
                        Spacer(modifier = Modifier.size(4.dp))
                    }
                    ValueRow(
                        color = expenseColor,
                        label = expenseLabel,
                        amount = sameIndexExpense.amount
                    )
                }

                if (sameIndexNetWorth != null) {
                    if ((sameIndexIncome != null && sameIndexIncome.amount > 0) ||
                        (sameIndexExpense != null && sameIndexExpense.amount > 0)
                    ) {
                        Spacer(modifier = Modifier.size(4.dp))
                    }
                    ValueRow(
                        color = netWorthColor,
                        label = netWorthLabel,
                        amount = sameIndexNetWorth.amount
                    )
                }
            }
        }
    }
}

@Composable
private fun ValueRow(color: Color, label: String, amount: Long) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
        ) {
            drawCircle(color = color)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$label: ${CurrencyUtils.paiseToRupeeString(amount)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
