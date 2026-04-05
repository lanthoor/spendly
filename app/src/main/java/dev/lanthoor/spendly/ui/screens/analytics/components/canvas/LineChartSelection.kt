package dev.lanthoor.spendly.ui.screens.analytics.components.canvas

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import dev.lanthoor.spendly.domain.model.LineChartEntry

internal data class SelectedLinePoint(
    val pointIndex: Int,
    val dataType: String,
    val position: Offset
)

internal fun resolveNearestLinePoint(
    tapOffset: Offset,
    canvasSize: Size,
    density: Density,
    incomeData: List<LineChartEntry>,
    expenseData: List<LineChartEntry>,
    netWorthData: List<LineChartEntry>
): SelectedLinePoint? {
    val paddingLeft = with(density) { 48.dp.toPx() }
    val paddingRight = with(density) { 16.dp.toPx() }
    val paddingTop = with(density) { 16.dp.toPx() }
    val paddingBottom = with(density) { 32.dp.toPx() }

    val maxValue = maxOf(
        incomeData.maxOfOrNull { it.amount } ?: 0L,
        expenseData.maxOfOrNull { it.amount } ?: 0L,
        netWorthData.maxOfOrNull { it.amount } ?: 0L
    )
    val minValue = minOf(
        incomeData.minOfOrNull { it.amount } ?: 0L,
        expenseData.minOfOrNull { it.amount } ?: 0L,
        netWorthData.minOfOrNull { it.amount } ?: 0L
    )
    val valueRange = maxValue - minValue
    if (valueRange <= 0L) return null

    val chartHeight = canvasSize.height - paddingTop - paddingBottom
    val chartWidth = canvasSize.width - paddingLeft - paddingRight

    val dataPointCount = maxOf(incomeData.size, expenseData.size, netWorthData.size)
    val xScale = ChartMath.calculateXScale(dataPointCount, chartWidth)
    val yScale = chartHeight / (valueRange * 1.1f)

    fun valueToY(value: Long): Float {
        return paddingTop + chartHeight - ((value - minValue) * yScale)
    }

    val allPoints = mutableListOf<Triple<Offset, Int, String>>()

    incomeData.forEachIndexed { index, entry ->
        val x = paddingLeft + ChartMath.indexToX(index, xScale)
        val y = valueToY(entry.amount)
        allPoints.add(Triple(Offset(x, y), index, "income"))
    }

    expenseData.forEachIndexed { index, entry ->
        val x = paddingLeft + ChartMath.indexToX(index, xScale)
        val y = valueToY(entry.amount)
        allPoints.add(Triple(Offset(x, y), index, "expense"))
    }

    netWorthData.forEachIndexed { index, entry ->
        val x = paddingLeft + ChartMath.indexToX(index, xScale)
        val y = valueToY(entry.amount)
        allPoints.add(Triple(Offset(x, y), index, "networth"))
    }

    val threshold = with(density) { 48.dp.toPx() }
    val nearestPoint = allPoints.minByOrNull { (offset, _, _) ->
        ChartMath.distance(tapOffset.x, tapOffset.y, offset.x, offset.y)
    } ?: return null

    val (offset, index, dataType) = nearestPoint
    val distance = ChartMath.distance(tapOffset.x, tapOffset.y, offset.x, offset.y)
    if (distance > threshold) return null

    return SelectedLinePoint(
        pointIndex = index,
        dataType = dataType,
        position = offset
    )
}
