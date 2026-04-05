package dev.lanthoor.spendly.ui.screens.analytics.components.canvas

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.dp
import dev.lanthoor.spendly.domain.model.LineChartEntry
import dev.lanthoor.spendly.utils.CurrencyUtils
import kotlin.math.max

internal fun DrawScope.drawLineChartGrid(
    gridColor: Color,
    paddingLeft: Float,
    paddingTop: Float,
    chartWidth: Float,
    chartHeight: Float
) {
    repeat(4) { i ->
        val y = paddingTop + (chartHeight / 4f) * (i + 1)
        drawLine(
            color = gridColor,
            start = Offset(paddingLeft, y),
            end = Offset(paddingLeft + chartWidth, y),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
        )
    }
}

internal fun DrawScope.drawLineChartYAxisLabels(
    minValue: Long,
    valueRange: Long,
    paddingLeft: Float,
    textMeasurer: TextMeasurer,
    textStyle: TextStyle,
    valueToY: (Long) -> Float
) {
    repeat(5) { i ->
        val value = minValue + (valueRange * i / 4)
        val y = valueToY(value)
        val text = CurrencyUtils.paiseToRupeeString(value, abbreviated = true)

        val measuredText = textMeasurer.measure(text, textStyle)
        drawText(
            textMeasurer = textMeasurer,
            text = text,
            style = textStyle,
            topLeft = Offset(
                paddingLeft - measuredText.size.width - 8.dp.toPx(),
                y - measuredText.size.height / 2f
            )
        )
    }
}

internal fun DrawScope.drawLineSeriesWithPoints(
    data: List<LineChartEntry>,
    color: Color,
    targetDataType: String,
    selectedPointIndex: Int?,
    selectedDataType: String?,
    entryAnimationProgress: Float,
    animatedSelection: Float,
    paddingLeft: Float,
    xScale: Float,
    valueToY: (Long) -> Float
) {
    if (data.isEmpty()) return

    val path = Path()
    val points = mutableListOf<Offset>()
    val pointsToDraw = (data.size * entryAnimationProgress).toInt().coerceAtLeast(1)

    data.take(pointsToDraw).forEachIndexed { index, entry ->
        val x = paddingLeft + ChartMath.indexToX(index, xScale)
        val y = valueToY(entry.amount)

        if (index == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
        points.add(Offset(x, y))
    }

    drawPath(
        path = path,
        color = color,
        alpha = entryAnimationProgress,
        style = Stroke(
            width = 2.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )

    points.forEachIndexed { index, point ->
        val isSelected = selectedPointIndex == index && selectedDataType == targetDataType
        val radius = if (isSelected) {
            3.dp.toPx() + (1.dp.toPx() * animatedSelection)
        } else {
            3.dp.toPx()
        }

        if (isSelected) {
            drawCircle(
                color = Color.White,
                radius = radius + 1.dp.toPx(),
                center = point,
                alpha = entryAnimationProgress
            )
        }

        drawCircle(
            color = color,
            radius = radius,
            center = point,
            alpha = entryAnimationProgress
        )
    }
}

internal fun DrawScope.drawLineChartXAxisLabels(
    incomeData: List<LineChartEntry>,
    expenseData: List<LineChartEntry>,
    netWorthData: List<LineChartEntry>,
    chartWidth: Float,
    paddingLeft: Float,
    paddingTop: Float,
    chartHeight: Float,
    xScale: Float,
    textMeasurer: TextMeasurer,
    textStyle: TextStyle
) {
    val displayData = when {
        netWorthData.isNotEmpty() -> netWorthData
        expenseData.isNotEmpty() -> expenseData
        else -> incomeData
    }
    if (displayData.isEmpty()) return

    val optimalLabelCount = ChartMath.calculateOptimalLabelCount(
        availableSpace = chartWidth,
        minSpacing = 60.dp.toPx(),
        maxLabels = displayData.size
    )

    val labelStep = max(1, displayData.size / optimalLabelCount)

    displayData.forEachIndexed { index, entry ->
        if (index % labelStep == 0 || index == displayData.size - 1) {
            val x = paddingLeft + ChartMath.indexToX(index, xScale)
            val measuredText = textMeasurer.measure(entry.dateLabel, textStyle)
            drawText(
                textMeasurer = textMeasurer,
                text = entry.dateLabel,
                style = textStyle,
                topLeft = Offset(
                    x - measuredText.size.width / 2f,
                    paddingTop + chartHeight + 8.dp.toPx()
                )
            )
        }
    }
}
