package `in`.co.spendly.ui.screens.analytics.components.canvas

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.co.spendly.R
import `in`.co.spendly.domain.model.PieChartEntry
import `in`.co.spendly.domain.model.PieSliceArc
import `in`.co.spendly.ui.components.IconMapper
import `in`.co.spendly.ui.screens.analytics.components.canvas.ChartGestureHandler.chartTapGesture
import `in`.co.spendly.utils.CurrencyUtils

/**
 * Custom Canvas-based donut pie chart with tap interaction.
 *
 * Features:
 * - Donut chart with 55% center hole for displaying details
 * - 2° gap between slices for visual separation
 * - Tap on slice to show category details in center
 * - Selected slice expands 10dp outward with 200ms animation
 * - Tap in center or outside to deselect
 *
 * @param data List of pie chart entries (sorted by amount descending)
 * @param modifier Modifier for the chart container
 */
@Composable
fun CustomPieChart(
    data: List<PieChartEntry>,
    modifier: Modifier = Modifier,
    enableEntryAnimation: Boolean = true
) {
    // State for selected slice
    var selectedSliceId by remember { mutableStateOf<Long?>(null) }

    // State for entry animation
    var entryAnimationProgress by remember { mutableStateOf(0f) }

    // Entry animation on first appearance
    LaunchedEffect(data, enableEntryAnimation) {
        if (enableEntryAnimation && data.isNotEmpty()) {
            entryAnimationProgress = 0f
            androidx.compose.animation.core.animate(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = tween(durationMillis = 600)
            ) { value, _ ->
                entryAnimationProgress = value
            }
        } else {
            entryAnimationProgress = 1f
        }
    }

    // Find selected slice data
    val selectedSlice = remember(selectedSliceId, data) {
        data.find { it.categoryId == selectedSliceId }
    }

    // Animation for selection highlight
    val animatedHighlight by animateFloatAsState(
        targetValue = if (selectedSliceId != null) 1f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = stringResource(R.string.pie_selection_animation)
    )

    // Calculate slice arcs for hit testing
    val sliceArcs = remember(data) {
        val arcs = mutableListOf<PieSliceArc>()
        var currentAngle = 0f

        data.forEachIndexed { index, entry ->
            val sweepAngle = (entry.percentage / 100f) * 360f - 2f // 2° gap
            arcs.add(
                PieSliceArc(
                    startAngle = currentAngle,
                    sweepAngle = sweepAngle,
                    dataIndex = index
                )
            )
            currentAngle += sweepAngle + 2f // Add gap
        }

        arcs
    }

    // Get background color outside of Canvas
    val backgroundColor = MaterialTheme.colorScheme.surfaceContainerLow

    val pieIntro = stringResource(R.string.analytics_pie_intro)
    val tapSlice = stringResource(R.string.msg_tap_slice_for_details)
    val currentlySelectedLabel = stringResource(R.string.msg_currently_selected)

    // Build accessibility description
    val accessibilityDescription =
        remember(data, selectedSliceId, pieIntro, tapSlice, currentlySelectedLabel) {
            buildString {
                append(pieIntro)
                data.forEach { entry ->
                    append(" ${entry.categoryName}: ${String.format("%.1f", entry.percentage)}%, ")
                    append("${CurrencyUtils.formatPaise(entry.amount)}. ")
                }
                if (selectedSliceId != null) {
                    val selected = data.find { it.categoryId == selectedSliceId }
                    if (selected != null) {
                        append(" $currentlySelectedLabel ${selected.categoryName}, ")
                        append("${selected.transactionCount} transactions")
                    }
                } else {
                    append(tapSlice)
                }
            }
        }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
            .semantics {
                contentDescription = accessibilityDescription
            },
        contentAlignment = Alignment.Center
    ) {
        // Canvas for pie chart with integrated gesture handling
        Canvas(
            modifier = Modifier
                .size(280.dp)
                .chartTapGesture(
                    key = data
                ) { tapOffset, canvasSize ->
                    // Calculate chart dimensions
                    val canvasSizeFloat = canvasSize.width // Use actual canvas size
                    val centerX = canvasSizeFloat / 2f
                    val centerY = canvasSizeFloat / 2f
                    val baseRadius = canvasSizeFloat / 2f * 0.85f
                    val innerRadius = baseRadius * 0.55f
                    // Calculate 10dp expansion - approximate density conversion
                    val expansionPx =
                        canvasSizeFloat / 280f * 10f * 3f // 10dp * approximate density
                    val outerRadius = baseRadius + expansionPx // Include expansion

                    // Find tapped slice
                    val sliceIndex = ChartMath.findSliceAtPoint(
                        tapX = tapOffset.x,
                        tapY = tapOffset.y,
                        centerX = centerX,
                        centerY = centerY,
                        innerRadius = innerRadius,
                        outerRadius = outerRadius,
                        sliceArcs = sliceArcs
                    )

                    // Update selection
                    selectedSliceId = if (sliceIndex != null) {
                        data[sliceIndex].categoryId
                    } else {
                        null
                    }
                }
        ) {
            val canvasSize = size.minDimension
            val centerX = size.width / 2f
            val centerY = size.height / 2f

            // Calculate radii
            val baseRadius = canvasSize / 2f * 0.85f
            val innerRadius = baseRadius * 0.55f
            val strokeWidth = baseRadius - innerRadius

            // Draw each slice
            var currentAngle = 0f

            data.forEach { entry ->
                val isSelected = entry.categoryId == selectedSliceId

                // Calculate radius with animation
                val radius = if (isSelected) {
                    baseRadius + (10.dp.toPx() * animatedHighlight)
                } else {
                    baseRadius
                }

                // Calculate sweep angle (with 2° gap)
                val baseSweepAngle = (entry.percentage / 100f) * 360f - 2f

                // Apply entry animation to sweep angle
                val sweepAngle = baseSweepAngle * entryAnimationProgress

                // Draw arc as donut segment
                drawArc(
                    color = entry.color,
                    startAngle = currentAngle - 90f, // -90 to start at 12 o'clock
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(
                        centerX - radius,
                        centerY - radius
                    ),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth),
                    alpha = entryAnimationProgress // Fade in as well
                )

                currentAngle += baseSweepAngle + 2f // Add gap (use base angle for positioning)
            }

            // Draw center hole to create donut effect
            drawCircle(
                color = backgroundColor,
                radius = innerRadius,
                center = Offset(centerX, centerY)
            )
        }

        // Center text content (selected category details)
        if (selectedSlice != null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Category icon
                Icon(
                    imageVector = IconMapper.getIcon(selectedSlice.categoryIcon),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = selectedSlice.color
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Category name
                Text(
                    text = selectedSlice.categoryName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Amount
                Text(
                    text = CurrencyUtils.paiseToRupeeString(selectedSlice.amount),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Transaction count
                Text(
                    text = pluralStringResource(
                        R.plurals.plurals_transactions,
                        selectedSlice.transactionCount,
                        selectedSlice.transactionCount
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            // Placeholder text when nothing is selected
            Text(
                text = stringResource(R.string.msg_tap_slice_for_details),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
