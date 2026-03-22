package `in`.co.spendly.ui.screens.analytics.components.canvas

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.co.spendly.R
import `in`.co.spendly.domain.model.LineChartEntry
import `in`.co.spendly.ui.screens.analytics.components.canvas.ChartGestureHandler.chartTapGesture
import `in`.co.spendly.utils.CurrencyUtils
import kotlin.math.max

/**
 * Custom Canvas-based multi-line chart with tap interaction.
 *
 * Features:
 * - Two lines: Income (green) and Expense (red)
 * - 2dp line width, 3dp data points (4dp when selected)
 * - Selected point highlighted with white 1dp stroke
 * - 4 horizontal dashed grid lines (25%, 50%, 75%, 100%)
 * - Y-axis labels with currency formatting (left side)
 * - X-axis labels with smart spacing based on available space
 * - Selection summary card below chart
 *
 * @param incomeData List of income data points (can be empty)
 * @param expenseData List of expense data points (can be empty)
 * @param modifier Modifier for the chart container
 */
@Composable
fun CustomLineChart(
    incomeData: List<LineChartEntry>,
    expenseData: List<LineChartEntry>,
    netWorthData: List<LineChartEntry>,
    modifier: Modifier = Modifier,
    enableEntryAnimation: Boolean = true
) {
    val incomeColor = Color(0xFF4CAF50) // Green for income
    val expenseColor = Color(0xFFF44336) // Red for expense
    val netWorthColor = Color(0xFF2196F3) // Blue for net worth
    val gridColor = MaterialTheme.colorScheme.outlineVariant

    // State for selected point
    var selectedPointIndex by remember { mutableStateOf<Int?>(null) }
    var selectedDataType by remember { mutableStateOf<String?>(null) } // "income" | "expense" | "networth"
    var selectedPointPosition by remember { mutableStateOf<Offset?>(null) }

    // State for entry animation (line drawing effect)
    var entryAnimationProgress by remember { mutableStateOf(0f) }

    // Entry animation on first appearance
    LaunchedEffect(incomeData, expenseData, netWorthData, enableEntryAnimation) {
        if (enableEntryAnimation && (incomeData.isNotEmpty() || expenseData.isNotEmpty() || netWorthData.isNotEmpty())) {
            entryAnimationProgress = 0f
            androidx.compose.animation.core.animate(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = tween(durationMillis = 800)
            ) { value, _ ->
                entryAnimationProgress = value
            }
        } else {
            entryAnimationProgress = 1f
        }
    }

    // Find selected point data
    val selectedPoint =
        remember(selectedPointIndex, selectedDataType, incomeData, expenseData, netWorthData) {
            when {
                selectedPointIndex == null || selectedDataType == null -> null
                selectedDataType == "income" && selectedPointIndex!! < incomeData.size -> incomeData[selectedPointIndex!!]
                selectedDataType == "expense" && selectedPointIndex!! < expenseData.size -> expenseData[selectedPointIndex!!]
                selectedDataType == "networth" && selectedPointIndex!! < netWorthData.size -> netWorthData[selectedPointIndex!!]
                else -> null
            }
        }

    // Animation for selection highlight
    val animatedSelection by animateFloatAsState(
        targetValue = if (selectedPointIndex != null) 1f else 0f,
        animationSpec = tween(durationMillis = 150),
        label = stringResource(R.string.line_selection_animation)
    )

    // Text measurer for labels
    val textMeasurer = rememberTextMeasurer()
    val textStyle = TextStyle(
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 10.sp
    )

    // Get colors outside of Canvas
    MaterialTheme.colorScheme.onSurfaceVariant

    val chartIntro = stringResource(R.string.analytics_chart_intro)
    val incomeLabel = stringResource(R.string.label_income)
    val expenseLabel = stringResource(R.string.label_expense)
    val netWorthLabel = stringResource(R.string.label_net_worth)
    val tapDetails = stringResource(R.string.msg_tap_point_for_details)
    val currentlySelectedLabel = stringResource(R.string.msg_currently_selected)

    // Build accessibility description
    val accessibilityDescription = remember(
        incomeData,
        expenseData,
        netWorthData,
        selectedPointIndex,
        selectedDataType,
        chartIntro,
        incomeLabel,
        expenseLabel,
        netWorthLabel,
        tapDetails,
        currentlySelectedLabel
    ) {
        buildString {
            append(chartIntro)
            if (incomeData.isNotEmpty()) {
                append(" $incomeLabel data: ${incomeData.size} points. ")
            }
            if (expenseData.isNotEmpty()) {
                append(" $expenseLabel data: ${expenseData.size} points. ")
            }
            if (netWorthData.isNotEmpty()) {
                append(" $netWorthLabel data: ${netWorthData.size} points. ")
            }
            val index = selectedPointIndex
            val dataType = selectedDataType
            if (index != null && dataType != null) {
                val point = when (dataType) {
                    "income" -> if (index < incomeData.size) incomeData[index] else null
                    "expense" -> if (index < expenseData.size) expenseData[index] else null
                    "networth" -> if (index < netWorthData.size) netWorthData[index] else null
                    else -> null
                }
                if (point != null) {
                    append(" $currentlySelectedLabel ${point.dateLabel}, ")
                    append("${CurrencyUtils.formatPaise(point.amount)}.")
                }
            } else {
                append(" $tapDetails")
            }
        }
    }

    val density = LocalDensity.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = accessibilityDescription
            }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Canvas for line chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .chartTapGesture(
                            key = listOf(incomeData, expenseData, netWorthData)
                        ) { tapOffset, canvasSize ->
                            // Calculate chart dimensions
                            val paddingLeft = with(density) { 48.dp.toPx() } // Y-axis labels
                            val paddingRight = with(density) { 16.dp.toPx() }
                            val paddingTop = with(density) { 16.dp.toPx() }
                            val paddingBottom = with(density) { 32.dp.toPx() } // X-axis labels

                            // Create list of all points with their positions
                            val allPoints = mutableListOf<Triple<Offset, Int, String>>()

                            // Add income points
                            if (incomeData.isNotEmpty()) {
                                val maxValue = maxOf(
                                    incomeData.maxOfOrNull { it.amount } ?: 0L,
                                    expenseData.maxOfOrNull { it.amount } ?: 0L,
                                    netWorthData.maxOfOrNull { it.amount } ?: 0L
                                )
                                minOf(
                                    incomeData.minOfOrNull { it.amount } ?: 0L,
                                    expenseData.minOfOrNull { it.amount } ?: 0L,
                                    netWorthData.minOfOrNull { it.amount } ?: 0L
                                )
                                val chartHeight = canvasSize.height - paddingTop - paddingBottom
                                val chartWidth = canvasSize.width - paddingLeft - paddingRight
                                val xScale = ChartMath.calculateXScale(incomeData.size, chartWidth)
                                val yScale = ChartMath.calculateYScale(maxValue, chartHeight, 0.1f)

                                incomeData.forEachIndexed { index, entry ->
                                    val x = paddingLeft + ChartMath.indexToX(index, xScale)
                                    val y = paddingTop + ChartMath.valueToY(
                                        entry.amount,
                                        chartHeight,
                                        maxValue,
                                        yScale
                                    )
                                    allPoints.add(Triple(Offset(x, y), index, "income"))
                                }
                            }

                            // Add expense points
                            if (expenseData.isNotEmpty()) {
                                val maxValue = maxOf(
                                    incomeData.maxOfOrNull { it.amount } ?: 0L,
                                    expenseData.maxOfOrNull { it.amount } ?: 0L,
                                    netWorthData.maxOfOrNull { it.amount } ?: 0L
                                )
                                minOf(
                                    incomeData.minOfOrNull { it.amount } ?: 0L,
                                    expenseData.minOfOrNull { it.amount } ?: 0L,
                                    netWorthData.minOfOrNull { it.amount } ?: 0L
                                )
                                val chartHeight = canvasSize.height - paddingTop - paddingBottom
                                val chartWidth = canvasSize.width - paddingLeft - paddingRight
                                val xScale = ChartMath.calculateXScale(expenseData.size, chartWidth)
                                val yScale = ChartMath.calculateYScale(maxValue, chartHeight, 0.1f)

                                expenseData.forEachIndexed { index, entry ->
                                    val x = paddingLeft + ChartMath.indexToX(index, xScale)
                                    val y = paddingTop + ChartMath.valueToY(
                                        entry.amount,
                                        chartHeight,
                                        maxValue,
                                        yScale
                                    )
                                    allPoints.add(Triple(Offset(x, y), index, "expense"))
                                }
                            }

                            // Add net worth points
                            if (netWorthData.isNotEmpty()) {
                                val maxValue = maxOf(
                                    incomeData.maxOfOrNull { it.amount } ?: 0L,
                                    expenseData.maxOfOrNull { it.amount } ?: 0L,
                                    netWorthData.maxOfOrNull { it.amount } ?: 0L
                                )
                                minOf(
                                    incomeData.minOfOrNull { it.amount } ?: 0L,
                                    expenseData.minOfOrNull { it.amount } ?: 0L,
                                    netWorthData.minOfOrNull { it.amount } ?: 0L
                                )
                                val chartHeight = canvasSize.height - paddingTop - paddingBottom
                                val chartWidth = canvasSize.width - paddingLeft - paddingRight
                                val xScale =
                                    ChartMath.calculateXScale(netWorthData.size, chartWidth)
                                val yScale = ChartMath.calculateYScale(maxValue, chartHeight, 0.1f)

                                netWorthData.forEachIndexed { index, entry ->
                                    val x = paddingLeft + ChartMath.indexToX(index, xScale)
                                    val y = paddingTop + ChartMath.valueToY(
                                        entry.amount,
                                        chartHeight,
                                        maxValue,
                                        yScale
                                    )
                                    allPoints.add(Triple(Offset(x, y), index, "networth"))
                                }
                            }

                            // Find nearest point
                            val threshold = with(density) { 48.dp.toPx() } // Touch target size
                            val nearestPoint = allPoints.minByOrNull { (offset, _, _) ->
                                ChartMath.distance(tapOffset.x, tapOffset.y, offset.x, offset.y)
                            }

                            if (nearestPoint != null) {
                                val (offset, index, dataType) = nearestPoint
                                val distance =
                                    ChartMath.distance(tapOffset.x, tapOffset.y, offset.x, offset.y)

                                if (distance <= threshold) {
                                    selectedPointIndex = index
                                    selectedDataType = dataType
                                    selectedPointPosition = offset
                                } else {
                                    selectedPointIndex = null
                                    selectedDataType = null
                                    selectedPointPosition = null
                                }
                            } else {
                                selectedPointIndex = null
                                selectedDataType = null
                                selectedPointPosition = null
                            }
                        }
                ) {
                    val paddingLeft = 48.dp.toPx() // Y-axis labels
                    val paddingRight = 16.dp.toPx()
                    val paddingTop = 16.dp.toPx()
                    val paddingBottom = 32.dp.toPx() // X-axis labels

                    val chartWidth = size.width - paddingLeft - paddingRight
                    val chartHeight = size.height - paddingTop - paddingBottom

                    // Calculate min/max values across all three datasets
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

                    if (maxValue == 0L && minValue == 0L) {
                        // Empty state - no data to render
                        return@Canvas
                    }

                    // Calculate scales
                    val dataPointCount = maxOf(incomeData.size, expenseData.size, netWorthData.size)
                    val xScale = ChartMath.calculateXScale(dataPointCount, chartWidth)

                    // Calculate Y scale based on full range (min to max)
                    val valueRange = maxValue - minValue
                    val yScale = if (valueRange > 0) {
                        chartHeight / (valueRange * 1.1f)  // 10% padding
                    } else {
                        1f
                    }

                    // Draw grid lines (4 horizontal dashed lines at 25%, 50%, 75%, 100%)
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

                    // Helper function to calculate Y position from value
                    fun valueToY(value: Long): Float {
                        return paddingTop + chartHeight - ((value - minValue) * yScale)
                    }

                    // Draw Y-axis labels (5 labels spanning min to max)
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

                    // Draw expense line
                    if (expenseData.isNotEmpty()) {
                        val expensePath = Path()
                        val expensePoints = mutableListOf<Offset>()

                        // Calculate how many points to draw based on animation progress
                        val pointsToDraw =
                            (expenseData.size * entryAnimationProgress).toInt().coerceAtLeast(1)

                        expenseData.take(pointsToDraw).forEachIndexed { index, entry ->
                            val x = paddingLeft + ChartMath.indexToX(index, xScale)
                            val y = valueToY(entry.amount)

                            if (index == 0) {
                                expensePath.moveTo(x, y)
                            } else {
                                expensePath.lineTo(x, y)
                            }
                            expensePoints.add(Offset(x, y))
                        }

                        // Draw line with fade-in
                        drawPath(
                            path = expensePath,
                            color = expenseColor,
                            alpha = entryAnimationProgress,
                            style = Stroke(
                                width = 2.dp.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )

                        // Draw data points (with fade-in)
                        expensePoints.forEachIndexed { index, point ->
                            val isSelected =
                                selectedPointIndex == index && selectedDataType == "expense"
                            val radius = if (isSelected) {
                                3.dp.toPx() + (1.dp.toPx() * animatedSelection)
                            } else {
                                3.dp.toPx()
                            }

                            // White stroke for selected point
                            if (isSelected) {
                                drawCircle(
                                    color = Color.White,
                                    radius = radius + 1.dp.toPx(),
                                    center = point,
                                    alpha = entryAnimationProgress
                                )
                            }

                            // Data point
                            drawCircle(
                                color = expenseColor,
                                radius = radius,
                                center = point,
                                alpha = entryAnimationProgress
                            )
                        }
                    }

                    // Draw income line
                    if (incomeData.isNotEmpty()) {
                        val incomePath = Path()
                        val incomePoints = mutableListOf<Offset>()

                        // Calculate how many points to draw based on animation progress
                        val pointsToDraw =
                            (incomeData.size * entryAnimationProgress).toInt().coerceAtLeast(1)

                        incomeData.take(pointsToDraw).forEachIndexed { index, entry ->
                            val x = paddingLeft + ChartMath.indexToX(index, xScale)
                            val y = valueToY(entry.amount)

                            if (index == 0) {
                                incomePath.moveTo(x, y)
                            } else {
                                incomePath.lineTo(x, y)
                            }
                            incomePoints.add(Offset(x, y))
                        }

                        // Draw line with fade-in
                        drawPath(
                            path = incomePath,
                            color = incomeColor,
                            alpha = entryAnimationProgress,
                            style = Stroke(
                                width = 2.dp.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )

                        // Draw data points (with fade-in)
                        incomePoints.forEachIndexed { index, point ->
                            val isSelected =
                                selectedPointIndex == index && selectedDataType == "income"
                            val radius = if (isSelected) {
                                3.dp.toPx() + (1.dp.toPx() * animatedSelection)
                            } else {
                                3.dp.toPx()
                            }

                            // White stroke for selected point
                            if (isSelected) {
                                drawCircle(
                                    color = Color.White,
                                    radius = radius + 1.dp.toPx(),
                                    center = point,
                                    alpha = entryAnimationProgress
                                )
                            }

                            // Data point
                            drawCircle(
                                color = incomeColor,
                                radius = radius,
                                center = point,
                                alpha = entryAnimationProgress
                            )
                        }
                    }

                    // Draw net worth line
                    if (netWorthData.isNotEmpty()) {
                        val netWorthPath = Path()
                        val netWorthPoints = mutableListOf<Offset>()

                        // Calculate how many points to draw based on animation progress
                        val pointsToDraw =
                            (netWorthData.size * entryAnimationProgress).toInt().coerceAtLeast(1)

                        netWorthData.take(pointsToDraw).forEachIndexed { index, entry ->
                            val x = paddingLeft + ChartMath.indexToX(index, xScale)
                            val y = valueToY(entry.amount)

                            if (index == 0) {
                                netWorthPath.moveTo(x, y)
                            } else {
                                netWorthPath.lineTo(x, y)
                            }
                            netWorthPoints.add(Offset(x, y))
                        }

                        // Draw line with fade-in
                        drawPath(
                            path = netWorthPath,
                            color = netWorthColor,
                            alpha = entryAnimationProgress,
                            style = Stroke(
                                width = 2.dp.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )

                        // Draw data points (with fade-in)
                        netWorthPoints.forEachIndexed { index, point ->
                            val isSelected =
                                selectedPointIndex == index && selectedDataType == "networth"
                            val radius = if (isSelected) {
                                3.dp.toPx() + (1.dp.toPx() * animatedSelection)
                            } else {
                                3.dp.toPx()
                            }

                            // White stroke for selected point
                            if (isSelected) {
                                drawCircle(
                                    color = Color.White,
                                    radius = radius + 1.dp.toPx(),
                                    center = point,
                                    alpha = entryAnimationProgress
                                )
                            }

                            // Data point
                            drawCircle(
                                color = netWorthColor,
                                radius = radius,
                                center = point,
                                alpha = entryAnimationProgress
                            )
                        }
                    }

                    // Draw X-axis labels (selective based on space)
                    // Prefer netWorthData as it contains all dates, fallback to expense or income
                    val displayData = when {
                        netWorthData.isNotEmpty() -> netWorthData
                        expenseData.isNotEmpty() -> expenseData
                        else -> incomeData
                    }
                    if (displayData.isNotEmpty()) {
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
                }
            }
        }  // Close Column

        // Smart-positioned popover for selected point
        selectedPoint?.let { point ->
            selectedPointPosition?.let { position ->
                // Get all three values for the selected date
                val sameIndexIncome =
                    if (selectedPointIndex!! < incomeData.size) incomeData[selectedPointIndex!!] else null
                val sameIndexExpense =
                    if (selectedPointIndex!! < expenseData.size) expenseData[selectedPointIndex!!] else null
                val sameIndexNetWorth =
                    if (selectedPointIndex!! < netWorthData.size) netWorthData[selectedPointIndex!!] else null

                // Popover dimensions
                val popoverWidth = 200.dp
                val popoverHeight = 120.dp  // Estimated height

                // Determine if popover should be above or below
                val shouldShowAbove = with(density) { position.y > popoverHeight.toPx() }

                // Calculate horizontal offset to center popover on point
                val horizontalOffset = with(density) {
                    (position.x - (popoverWidth.toPx() / 2f)).coerceIn(
                        16.dp.toPx(),  // Min left margin
                        280.dp.toPx() - popoverWidth.toPx() - 16.dp.toPx()  // Max right edge (chart width - popover - margin)
                    )
                }

                Box(
                    modifier = Modifier
                        .offset {
                            with(density) {
                                IntOffset(
                                    x = horizontalOffset.toInt(),
                                    y = if (shouldShowAbove) {
                                        (position.y - popoverHeight.toPx() - 16.dp.toPx()).toInt()
                                    } else {
                                        (position.y + 24.dp.toPx()).toInt()
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
                            // Date label
                            Text(
                                text = point.dateLabel,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Income row (if exists)
                            if (sameIndexIncome != null && sameIndexIncome.amount > 0) {
                                ValueRow(
                                    color = incomeColor,
                                    label = incomeLabel,
                                    amount = sameIndexIncome.amount
                                )
                            }

                            // Expense row (if exists)
                            if (sameIndexExpense != null && sameIndexExpense.amount > 0) {
                                if (sameIndexIncome != null && sameIndexIncome.amount > 0) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                                ValueRow(
                                    color = expenseColor,
                                    label = expenseLabel,
                                    amount = sameIndexExpense.amount
                                )
                            }

                            // Net Worth row (always show)
                            if (sameIndexNetWorth != null) {
                                if ((sameIndexIncome != null && sameIndexIncome.amount > 0) ||
                                    (sameIndexExpense != null && sameIndexExpense.amount > 0)
                                ) {
                                    Spacer(modifier = Modifier.height(4.dp))
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
