package dev.lanthoor.spendly.ui.screens.analytics.components.canvas

import androidx.compose.ui.geometry.Offset
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Mathematical utilities for custom chart rendering and hit testing.
 * All angles are in degrees, with 0° at 12 o'clock (top), rotating clockwise.
 */
object ChartMath {

    /**
     * Calculates the Euclidean distance between two points.
     *
     * @param x1 X-coordinate of first point
     * @param y1 Y-coordinate of first point
     * @param x2 X-coordinate of second point
     * @param y2 Y-coordinate of second point
     * @return Distance in pixels
     */
    fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        return sqrt((x2 - x1).pow(2) + (y2 - y1).pow(2))
    }

    /**
     * Calculates the angle from center point to target point.
     * Returns angle in degrees, normalized to 0-360° range.
     * 0° = 12 o'clock (top), rotating clockwise.
     *
     * @param tapX X-coordinate of tap point
     * @param tapY Y-coordinate of tap point
     * @param centerX X-coordinate of center point
     * @param centerY Y-coordinate of center point
     * @return Angle in degrees (0-360)
     */
    fun calculateAngle(tapX: Float, tapY: Float, centerX: Float, centerY: Float): Float {
        // atan2 returns angle in radians, with 0° at 3 o'clock (right), counter-clockwise
        val angleRadians = atan2(tapY - centerY, tapX - centerX)
        var angleDegrees = Math.toDegrees(angleRadians.toDouble()).toFloat()

        // Convert to our coordinate system: 0° at 12 o'clock, clockwise
        angleDegrees = (angleDegrees + 90f + 360f) % 360f

        return angleDegrees
    }

    /**
     * Checks if a point is within a donut ring (annulus).
     *
     * @param tapX X-coordinate of tap point
     * @param tapY Y-coordinate of tap point
     * @param centerX X-coordinate of donut center
     * @param centerY Y-coordinate of donut center
     * @param innerRadius Inner radius of donut (hole)
     * @param outerRadius Outer radius of donut
     * @return true if point is within the ring
     */
    fun isInDonutRing(
        tapX: Float,
        tapY: Float,
        centerX: Float,
        centerY: Float,
        innerRadius: Float,
        outerRadius: Float
    ): Boolean {
        val dist = distance(tapX, tapY, centerX, centerY)
        return dist >= innerRadius && dist <= outerRadius
    }

    /**
     * Normalizes a value from one range to another.
     *
     * @param value Value to normalize
     * @param fromMin Minimum of source range
     * @param fromMax Maximum of source range
     * @param toMin Minimum of target range (default 0)
     * @param toMax Maximum of target range (default 1)
     * @return Normalized value
     */
    fun normalize(
        value: Float,
        fromMin: Float,
        fromMax: Float,
        toMin: Float = 0f,
        toMax: Float = 1f
    ): Float {
        if (fromMax == fromMin) return toMin
        val normalized = (value - fromMin) / (fromMax - fromMin)
        return toMin + normalized * (toMax - toMin)
    }

    /**
     * Finds the pie slice at a given tap point.
     * Returns the index of the slice, or null if no slice was hit.
     *
     * @param tapX X-coordinate of tap point
     * @param tapY Y-coordinate of tap point
     * @param centerX X-coordinate of pie center
     * @param centerY Y-coordinate of pie center
     * @param innerRadius Inner radius (for donut chart)
     * @param outerRadius Outer radius
     * @param sliceArcs List of arc definitions for each slice
     * @return Index of the tapped slice, or null if outside all slices
     */
    fun findSliceAtPoint(
        tapX: Float,
        tapY: Float,
        centerX: Float,
        centerY: Float,
        innerRadius: Float,
        outerRadius: Float,
        sliceArcs: List<PieSliceArc>
    ): Int? {
        // First check if tap is within the donut ring
        if (!isInDonutRing(tapX, tapY, centerX, centerY, innerRadius, outerRadius)) {
            return null
        }

        // Calculate tap angle
        val tapAngle = calculateAngle(tapX, tapY, centerX, centerY)

        // Find which slice contains this angle
        sliceArcs.forEach { arc ->
            val sliceStartAngle = arc.startAngle
            val sliceEndAngle = (arc.startAngle + arc.sweepAngle) % 360f

            // Handle angle wrapping around 360°/0°
            val isInSlice = if (sliceEndAngle < sliceStartAngle) {
                // Slice wraps around 0° (e.g., 350° to 10°)
                tapAngle >= sliceStartAngle || tapAngle <= sliceEndAngle
            } else {
                // Normal case
                tapAngle >= sliceStartAngle && tapAngle <= sliceEndAngle
            }

            if (isInSlice) {
                return arc.dataIndex
            }
        }

        return null
    }

    /**
     * Finds the nearest point to a tap location within a threshold.
     * Returns the index of the nearest point, or null if all points are too far.
     *
     * @param tapX X-coordinate of tap point
     * @param tapY Y-coordinate of tap point
     * @param points List of chart points
     * @param threshold Maximum distance in pixels (default 48dp for touch target)
     * @return Index of nearest point, or null if none within threshold
     */
    fun findNearestPoint(
        tapX: Float,
        tapY: Float,
        points: List<Offset>,
        threshold: Float = 48f
    ): Int? {
        var nearestIndex: Int? = null
        var minDistance = Float.MAX_VALUE

        points.forEachIndexed { index, point ->
            val dist = distance(tapX, tapY, point.x, point.y)
            if (dist < minDistance && dist <= threshold) {
                minDistance = dist
                nearestIndex = index
            }
        }

        return nearestIndex
    }

    /**
     * Calculates optimal number of labels that can fit in available space.
     *
     * @param availableSpace Total space in pixels
     * @param minSpacing Minimum spacing between labels in pixels
     * @param maxLabels Maximum number of labels to show
     * @return Optimal number of labels
     */
    fun calculateOptimalLabelCount(
        availableSpace: Float,
        minSpacing: Float,
        maxLabels: Int
    ): Int {
        val maxFit = (availableSpace / minSpacing).toInt()
        return maxFit.coerceIn(1, maxLabels)
    }

    /**
     * Calculates scale factor for Y-axis to fit all values with padding.
     *
     * @param maxValue Maximum value in the dataset
     * @param chartHeight Available height in pixels
     * @param topPadding Top padding for visual spacing (default 0.1 = 10%)
     * @return Scale factor (pixels per unit value)
     */
    fun calculateYScale(
        maxValue: Long,
        chartHeight: Float,
        topPadding: Float = 0.1f
    ): Float {
        if (maxValue == 0L) return 1f
        return chartHeight / (maxValue * (1f + topPadding))
    }

    /**
     * Calculates scale factor for X-axis to fit all data points.
     *
     * @param dataPointCount Number of data points
     * @param chartWidth Available width in pixels
     * @return Scale factor (pixels between points)
     */
    fun calculateXScale(
        dataPointCount: Int,
        chartWidth: Float
    ): Float {
        if (dataPointCount <= 1) return 0f
        return chartWidth / (dataPointCount - 1)
    }

    /**
     * Converts a value to Y-coordinate in chart space.
     * Higher values result in lower Y coordinates (top of chart).
     *
     * @param value Data value in paise
     * @param chartHeight Total chart height
     * @param maxValue Maximum value in dataset
     * @param yScale Precalculated Y scale factor
     * @return Y-coordinate in pixels from top
     */
    fun valueToY(
        value: Long,
        chartHeight: Float,
        maxValue: Long,
        yScale: Float
    ): Float {
        return chartHeight - (value * yScale)
    }

    /**
     * Converts a data point index to X-coordinate in chart space.
     *
     * @param index Data point index
     * @param xScale Precalculated X scale factor
     * @return X-coordinate in pixels from left
     */
    fun indexToX(
        index: Int,
        xScale: Float
    ): Float {
        return index * xScale
    }
}
