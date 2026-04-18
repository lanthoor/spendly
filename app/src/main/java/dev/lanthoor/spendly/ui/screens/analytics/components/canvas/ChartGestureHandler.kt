package dev.lanthoor.spendly.ui.screens.analytics.components.canvas

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput

/**
 * Gesture handling utilities for custom charts.
 * Provides reusable modifiers for tap detection and selection handling.
 */
object ChartGestureHandler {

    /**
     * Creates a modifier that handles tap gestures for chart selection.
     * Calls the onTap callback with the tap offset and canvas size when a tap is detected.
     *
     * @param key Unique key for gesture detection (use data or selection state)
     * @param onTap Callback invoked with tap offset and canvas size when chart is tapped
     * @return Modifier with tap gesture handling
     */
    fun Modifier.chartTapGesture(
        key: Any,
        onTap: (tapOffset: Offset, canvasSize: Size) -> Unit
    ): Modifier {
        return this.pointerInput(key) {
            detectTapGestures { offset ->
                onTap(offset, Size(size.width.toFloat(), size.height.toFloat()))
            }
        }
    }

    /**
     * Creates a modifier that handles pie chart tap gestures.
     * Automatically performs hit testing and calls onSliceSelected with the slice index.
     *
     * @param key Unique key for gesture detection
     * @param centerX X-coordinate of pie center
     * @param centerY Y-coordinate of pie center
     * @param innerRadius Inner radius of donut
     * @param outerRadius Outer radius of donut
     * @param sliceArcs List of arc definitions
     * @param onSliceSelected Callback invoked with slice index (or null if no slice hit)
     * @return Modifier with pie chart tap handling
     */
    fun Modifier.pieChartTapGesture(
        key: Any,
        centerX: Float,
        centerY: Float,
        innerRadius: Float,
        outerRadius: Float,
        sliceArcs: List<PieSliceArc>,
        onSliceSelected: (Int?) -> Unit
    ): Modifier {
        return this.pointerInput(key) {
            detectTapGestures { offset ->
                val sliceIndex = ChartMath.findSliceAtPoint(
                    tapX = offset.x,
                    tapY = offset.y,
                    centerX = centerX,
                    centerY = centerY,
                    innerRadius = innerRadius,
                    outerRadius = outerRadius,
                    sliceArcs = sliceArcs
                )
                onSliceSelected(sliceIndex)
            }
        }
    }

    /**
     * Creates a modifier that handles line chart tap gestures.
     * Automatically performs hit testing and calls onPointSelected with the point index.
     *
     * @param key Unique key for gesture detection
     * @param points List of chart point positions
     * @param threshold Maximum distance for selection (default 48dp for touch target)
     * @param onPointSelected Callback invoked with point index (or null if no point hit)
     * @return Modifier with line chart tap handling
     */
    fun Modifier.lineChartTapGesture(
        key: Any,
        points: List<Offset>,
        threshold: Float = 48f,
        onPointSelected: (Int?) -> Unit
    ): Modifier {
        return this.pointerInput(key) {
            detectTapGestures { offset ->
                val pointIndex = ChartMath.findNearestPoint(
                    tapX = offset.x,
                    tapY = offset.y,
                    points = points,
                    threshold = threshold
                )
                onPointSelected(pointIndex)
            }
        }
    }
}
