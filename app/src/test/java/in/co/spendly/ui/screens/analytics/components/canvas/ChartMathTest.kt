package `in`.co.spendly.ui.screens.analytics.components.canvas

import androidx.compose.ui.geometry.Offset
import `in`.co.spendly.domain.model.PieSliceArc
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for ChartMath utility functions.
 * Tests coordinate calculations, hit testing, and edge cases.
 */
class ChartMathTest {

    @Test
    fun `distance calculates correctly`() {
        // Horizontal distance
        assertEquals(10f, ChartMath.distance(0f, 0f, 10f, 0f), 0.001f)

        // Vertical distance
        assertEquals(10f, ChartMath.distance(0f, 0f, 0f, 10f), 0.001f)

        // Diagonal distance (3-4-5 triangle)
        assertEquals(5f, ChartMath.distance(0f, 0f, 3f, 4f), 0.001f)

        // Zero distance
        assertEquals(0f, ChartMath.distance(5f, 5f, 5f, 5f), 0.001f)
    }

    @Test
    fun `calculateAngle returns correct angles`() {
        val centerX = 100f
        val centerY = 100f

        // Top (12 o'clock) = 0°
        assertEquals(0f, ChartMath.calculateAngle(100f, 50f, centerX, centerY), 1f)

        // Right (3 o'clock) = 90°
        assertEquals(90f, ChartMath.calculateAngle(150f, 100f, centerX, centerY), 1f)

        // Bottom (6 o'clock) = 180°
        assertEquals(180f, ChartMath.calculateAngle(100f, 150f, centerX, centerY), 1f)

        // Left (9 o'clock) = 270°
        assertEquals(270f, ChartMath.calculateAngle(50f, 100f, centerX, centerY), 1f)
    }

    @Test
    fun `isInDonutRing detects points correctly`() {
        val centerX = 100f
        val centerY = 100f
        val innerRadius = 30f
        val outerRadius = 50f

        // Point in ring
        assertTrue(ChartMath.isInDonutRing(100f, 140f, centerX, centerY, innerRadius, outerRadius))

        // Point in center hole
        assertFalse(ChartMath.isInDonutRing(100f, 110f, centerX, centerY, innerRadius, outerRadius))

        // Point outside ring
        assertFalse(ChartMath.isInDonutRing(100f, 200f, centerX, centerY, innerRadius, outerRadius))

        // Point exactly on inner radius (boundary - should be true)
        assertTrue(
            ChartMath.isInDonutRing(
                100f,
                100f + innerRadius,
                centerX,
                centerY,
                innerRadius,
                outerRadius
            )
        )

        // Point exactly on outer radius (boundary - should be true)
        assertTrue(
            ChartMath.isInDonutRing(
                100f,
                100f + outerRadius,
                centerX,
                centerY,
                innerRadius,
                outerRadius
            )
        )
    }

    @Test
    fun `normalize scales values correctly`() {
        // 0-100 to 0-1
        assertEquals(0f, ChartMath.normalize(0f, 0f, 100f, 0f, 1f), 0.001f)
        assertEquals(0.5f, ChartMath.normalize(50f, 0f, 100f, 0f, 1f), 0.001f)
        assertEquals(1f, ChartMath.normalize(100f, 0f, 100f, 0f, 1f), 0.001f)

        // 0-100 to 0-10
        assertEquals(5f, ChartMath.normalize(50f, 0f, 100f, 0f, 10f), 0.001f)

        // 10-20 to 0-1
        assertEquals(0.5f, ChartMath.normalize(15f, 10f, 20f, 0f, 1f), 0.001f)

        // Same min/max should return toMin
        assertEquals(0f, ChartMath.normalize(50f, 50f, 50f, 0f, 1f), 0.001f)
    }

    @Test
    fun `findSliceAtPoint detects correct slice`() {
        val centerX = 100f
        val centerY = 100f
        val innerRadius = 30f
        val outerRadius = 50f

        // Create 4 equal slices (90° each)
        val slices = listOf(
            PieSliceArc(startAngle = 0f, sweepAngle = 90f, dataIndex = 0),      // 0-90° (top-right)
            PieSliceArc(
                startAngle = 90f,
                sweepAngle = 90f,
                dataIndex = 1
            ),     // 90-180° (bottom-right)
            PieSliceArc(
                startAngle = 180f,
                sweepAngle = 90f,
                dataIndex = 2
            ),    // 180-270° (bottom-left)
            PieSliceArc(
                startAngle = 270f,
                sweepAngle = 90f,
                dataIndex = 3
            )     // 270-360° (top-left)
        )

        // Tap at top-right (45°) -> slice 0
        assertEquals(
            0,
            ChartMath.findSliceAtPoint(
                135f,
                65f,
                centerX,
                centerY,
                innerRadius,
                outerRadius,
                slices
            )
        )

        // Tap at bottom-right (135°) -> slice 1
        assertEquals(
            1,
            ChartMath.findSliceAtPoint(
                135f,
                135f,
                centerX,
                centerY,
                innerRadius,
                outerRadius,
                slices
            )
        )

        // Tap at bottom-left (225°) -> slice 2
        assertEquals(
            2,
            ChartMath.findSliceAtPoint(
                65f,
                135f,
                centerX,
                centerY,
                innerRadius,
                outerRadius,
                slices
            )
        )

        // Tap at top-left (315°) -> slice 3
        assertEquals(
            3,
            ChartMath.findSliceAtPoint(65f, 65f, centerX, centerY, innerRadius, outerRadius, slices)
        )

        // Tap in center (not in ring) -> null
        assertNull(
            ChartMath.findSliceAtPoint(
                100f,
                100f,
                centerX,
                centerY,
                innerRadius,
                outerRadius,
                slices
            )
        )

        // Tap outside ring -> null
        assertNull(
            ChartMath.findSliceAtPoint(
                100f,
                200f,
                centerX,
                centerY,
                innerRadius,
                outerRadius,
                slices
            )
        )
    }

    @Test
    fun `findSliceAtPoint handles angle wrapping`() {
        val centerX = 100f
        val centerY = 100f
        val innerRadius = 30f
        val outerRadius = 50f

        // Create slice that wraps around 360°/0°
        val slices = listOf(
            PieSliceArc(
                startAngle = 350f,
                sweepAngle = 20f,
                dataIndex = 0
            )  // 350° to 10° (wraps around)
        )

        // Tap at 355° (within wrapped slice)
        assertEquals(
            0,
            ChartMath.findSliceAtPoint(
                102f,
                51f,
                centerX,
                centerY,
                innerRadius,
                outerRadius,
                slices
            )
        )

        // Tap at 5° (within wrapped slice)
        assertEquals(
            0,
            ChartMath.findSliceAtPoint(
                102f,
                52f,
                centerX,
                centerY,
                innerRadius,
                outerRadius,
                slices
            )
        )

        // Tap at 180° (outside slice)
        assertNull(
            ChartMath.findSliceAtPoint(
                100f,
                140f,
                centerX,
                centerY,
                innerRadius,
                outerRadius,
                slices
            )
        )
    }

    @Test
    fun `findNearestPoint detects correct point`() {
        val points = listOf(
            Offset(0f, 0f),
            Offset(50f, 0f),
            Offset(100f, 0f),
            Offset(150f, 0f)
        )

        // Tap exactly on point 0
        assertEquals(0, ChartMath.findNearestPoint(0f, 0f, points, 48f))

        // Tap near point 1
        assertEquals(1, ChartMath.findNearestPoint(52f, 2f, points, 48f))

        // Tap near point 2
        assertEquals(2, ChartMath.findNearestPoint(98f, 0f, points, 48f))

        // Tap far from all points (beyond threshold)
        assertNull(ChartMath.findNearestPoint(0f, 100f, points, 48f))

        // Tap between points 1 and 2 - should select nearest
        assertEquals(1, ChartMath.findNearestPoint(60f, 0f, points, 48f))
    }

    @Test
    fun `findNearestPoint respects threshold`() {
        val points = listOf(
            Offset(0f, 0f),
            Offset(100f, 0f)
        )

        // Within threshold from point 0
        assertEquals(0, ChartMath.findNearestPoint(10f, 0f, points, 48f))

        // Outside threshold from all points (50f from each point, threshold is 48f)
        assertNull(ChartMath.findNearestPoint(50f, 0f, points, 48f))

        // Just beyond threshold from point 0 (49f away, threshold is 48f)
        assertNull(ChartMath.findNearestPoint(49f, 0f, points, 48f))

        // Exactly at threshold boundary from point 0 (48f away) - should be included
        assertEquals(0, ChartMath.findNearestPoint(48f, 0f, points, 48f))
    }

    @Test
    fun `calculateOptimalLabelCount returns sensible values`() {
        // Space for 10 labels with 50px spacing
        assertEquals(10, ChartMath.calculateOptimalLabelCount(500f, 50f, 12))

        // Space for 15 labels but max is 10
        assertEquals(10, ChartMath.calculateOptimalLabelCount(750f, 50f, 10))

        // Space for less than 1 label - should return 1
        assertEquals(1, ChartMath.calculateOptimalLabelCount(30f, 50f, 10))

        // Large space should not exceed max
        assertEquals(20, ChartMath.calculateOptimalLabelCount(10000f, 50f, 20))
    }

    @Test
    fun `calculateYScale handles edge cases`() {
        val chartHeight = 300f

        // Normal case
        val scale1 = ChartMath.calculateYScale(1000L, chartHeight, 0.1f)
        assertTrue(scale1 > 0f)

        // Zero max value - should return 1f
        assertEquals(1f, ChartMath.calculateYScale(0L, chartHeight, 0.1f), 0.001f)

        // With 10% padding, maxValue should fit in 90% of height
        val scale2 = ChartMath.calculateYScale(100L, 100f, 0.1f)
        val scaledValue = 100L * scale2
        assertTrue(scaledValue <= 100f) // Should fit in available space
    }

    @Test
    fun `calculateXScale handles edge cases`() {
        val chartWidth = 300f

        // Normal case - 5 points
        assertEquals(75f, ChartMath.calculateXScale(5, chartWidth), 0.001f)

        // Single point - should return 0
        assertEquals(0f, ChartMath.calculateXScale(1, chartWidth), 0.001f)

        // Two points - full width apart
        assertEquals(300f, ChartMath.calculateXScale(2, chartWidth), 0.001f)
    }

    @Test
    fun `valueToY converts values correctly`() {
        val chartHeight = 100f
        val maxValue = 1000L
        val yScale = ChartMath.calculateYScale(maxValue, chartHeight, 0f)

        // Max value should be at top (y=0)
        assertTrue(ChartMath.valueToY(maxValue, chartHeight, maxValue, yScale) < 10f)

        // Zero value should be at bottom (y=chartHeight)
        assertEquals(chartHeight, ChartMath.valueToY(0L, chartHeight, maxValue, yScale), 0.001f)

        // Half value should be in middle
        val midY = ChartMath.valueToY(500L, chartHeight, maxValue, yScale)
        assertTrue(midY > 40f && midY < 60f)
    }

    @Test
    fun `indexToX converts indices correctly`() {
        val xScale = 50f

        // First point at x=0
        assertEquals(0f, ChartMath.indexToX(0, xScale), 0.001f)

        // Second point at x=50
        assertEquals(50f, ChartMath.indexToX(1, xScale), 0.001f)

        // Fifth point at x=200
        assertEquals(200f, ChartMath.indexToX(4, xScale), 0.001f)
    }
}
