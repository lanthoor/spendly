package dev.lanthoor.spendly.ui.screens.analytics.components.canvas

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lanthoor.spendly.R
import dev.lanthoor.spendly.domain.model.LineChartEntry
import dev.lanthoor.spendly.ui.screens.analytics.components.canvas.ChartGestureHandler.chartTapGesture
import dev.lanthoor.spendly.utils.CurrencyUtils
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

    val incomeLabel = stringResource(R.string.label_income)
    val expenseLabel = stringResource(R.string.label_expense)
    val netWorthLabel = stringResource(R.string.label_net_worth)
    val accessibilityDescription = rememberLineChartAccessibilityDescription(
        incomeData = incomeData,
        expenseData = expenseData,
        netWorthData = netWorthData,
        selectedPointIndex = selectedPointIndex,
        selectedDataType = selectedDataType
    )

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
                            val selected = resolveNearestLinePoint(
                                tapOffset = tapOffset,
                                canvasSize = canvasSize,
                                density = density,
                                incomeData = incomeData,
                                expenseData = expenseData,
                                netWorthData = netWorthData
                            )

                            selectedPointIndex = selected?.pointIndex
                            selectedDataType = selected?.dataType
                            selectedPointPosition = selected?.position
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

                    drawLineChartGrid(
                        gridColor = gridColor,
                        paddingLeft = paddingLeft,
                        paddingTop = paddingTop,
                        chartWidth = chartWidth,
                        chartHeight = chartHeight
                    )

                    // Helper function to calculate Y position from value
                    fun valueToY(value: Long): Float {
                        return paddingTop + chartHeight - ((value - minValue) * yScale)
                    }

                    drawLineChartYAxisLabels(
                        minValue = minValue,
                        valueRange = valueRange,
                        paddingLeft = paddingLeft,
                        textMeasurer = textMeasurer,
                        textStyle = textStyle,
                        valueToY = ::valueToY
                    )

                    drawLineSeriesWithPoints(
                        data = expenseData,
                        color = expenseColor,
                        targetDataType = "expense",
                        selectedPointIndex = selectedPointIndex,
                        selectedDataType = selectedDataType,
                        entryAnimationProgress = entryAnimationProgress,
                        animatedSelection = animatedSelection,
                        paddingLeft = paddingLeft,
                        xScale = xScale,
                        valueToY = ::valueToY
                    )

                    drawLineSeriesWithPoints(
                        data = incomeData,
                        color = incomeColor,
                        targetDataType = "income",
                        selectedPointIndex = selectedPointIndex,
                        selectedDataType = selectedDataType,
                        entryAnimationProgress = entryAnimationProgress,
                        animatedSelection = animatedSelection,
                        paddingLeft = paddingLeft,
                        xScale = xScale,
                        valueToY = ::valueToY
                    )

                    drawLineSeriesWithPoints(
                        data = netWorthData,
                        color = netWorthColor,
                        targetDataType = "networth",
                        selectedPointIndex = selectedPointIndex,
                        selectedDataType = selectedDataType,
                        entryAnimationProgress = entryAnimationProgress,
                        animatedSelection = animatedSelection,
                        paddingLeft = paddingLeft,
                        xScale = xScale,
                        valueToY = ::valueToY
                    )

                    drawLineChartXAxisLabels(
                        incomeData = incomeData,
                        expenseData = expenseData,
                        netWorthData = netWorthData,
                        chartWidth = chartWidth,
                        paddingLeft = paddingLeft,
                        paddingTop = paddingTop,
                        chartHeight = chartHeight,
                        xScale = xScale,
                        textMeasurer = textMeasurer,
                        textStyle = textStyle
                    )
                }
            }
        }  // Close Column

        selectedPoint?.let { point ->
            selectedPointPosition?.let { position ->
                selectedPointIndex?.let { index ->
                    LineChartTooltip(
                        point = point,
                        pointIndex = index,
                        pointPosition = position,
                        incomeData = incomeData,
                        expenseData = expenseData,
                        netWorthData = netWorthData,
                        incomeColor = incomeColor,
                        expenseColor = expenseColor,
                        netWorthColor = netWorthColor,
                        incomeLabel = incomeLabel,
                        expenseLabel = expenseLabel,
                        netWorthLabel = netWorthLabel
                    )
                }
            }
        }
    }
}
