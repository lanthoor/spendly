package `in`.co.spendly.utils

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for YearType enum.
 *
 * Tests year start calculation logic for both Calendar and Financial years.
 * Pattern: methodName_inputCondition_expectedResult
 */
@RunWith(AndroidJUnit4::class)
class YearTypeEnumTest {

    @Test
    fun calendarYear_getYearStart_alwaysReturnsJanuaryOfSameYear() {
        // Test various months
        val testCases = listOf(
            Triple(2025, 1, Pair(2025, 1)),   // January
            Triple(2025, 6, Pair(2025, 1)),   // June
            Triple(2025, 12, Pair(2025, 1))   // December
        )

        testCases.forEach { (year, month, expected) ->
            val result = YearType.CALENDAR.getYearStart(year, month)
            assertEquals("Calendar year start for $month/$year", expected, result)
        }
    }

    @Test
    fun financialYear_getYearStart_janToMarch_returnsPreviousYearApril() {
        // Jan-Mar should return previous year's April
        val testCases = listOf(
            Triple(2025, 1, Pair(2024, 4)),   // Jan 2025 -> Apr 2024
            Triple(2025, 2, Pair(2024, 4)),   // Feb 2025 -> Apr 2024
            Triple(2025, 3, Pair(2024, 4))    // Mar 2025 -> Apr 2024
        )

        testCases.forEach { (year, month, expected) ->
            val result = YearType.FINANCIAL.getYearStart(year, month)
            assertEquals("Financial year start for $month/$year", expected, result)
        }
    }

    @Test
    fun financialYear_getYearStart_aprToDecember_returnsCurrentYearApril() {
        // Apr-Dec should return current year's April
        val testCases = listOf(
            Triple(2025, 4, Pair(2025, 4)),    // Apr 2025 -> Apr 2025
            Triple(2025, 6, Pair(2025, 4)),    // Jun 2025 -> Apr 2025
            Triple(2025, 12, Pair(2025, 4))    // Dec 2025 -> Apr 2025
        )

        testCases.forEach { (year, month, expected) ->
            val result = YearType.FINANCIAL.getYearStart(year, month)
            assertEquals("Financial year start for $month/$year", expected, result)
        }
    }

    @Test
    fun yearType_toDisplayName_returnsCorrectString() {
        assertEquals("Calendar Year", YearType.CALENDAR.toDisplayName())
        assertEquals("Financial Year", YearType.FINANCIAL.toDisplayName())
    }

    @Test
    fun yearType_getDisplayRange_returnsCorrectRange() {
        assertEquals("January - December", YearType.CALENDAR.getDisplayRange())
        assertEquals("April - March", YearType.FINANCIAL.getDisplayRange())
    }

    @Test
    fun yearType_fromString_parsesCorrectly() {
        assertEquals(YearType.CALENDAR, YearType.fromString("CALENDAR"))
        assertEquals(YearType.FINANCIAL, YearType.fromString("FINANCIAL"))
        assertNull(YearType.fromString("INVALID"))
    }

    @Test
    fun yearType_fromStringOrDefault_usesDefaultForInvalid() {
        assertEquals(YearType.CALENDAR, YearType.fromStringOrDefault("INVALID"))
        assertEquals(
            YearType.FINANCIAL,
            YearType.fromStringOrDefault("INVALID", YearType.FINANCIAL)
        )
        assertEquals(YearType.CALENDAR, YearType.fromStringOrDefault("CALENDAR"))
    }
}
