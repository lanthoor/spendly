package `in`.co.spendly.ui.screens.analytics

/**
 * Analytics period type for segmented button selection.
 */
enum class AnalyticsPeriodType {
    FINANCIAL_YEAR,
    CALENDAR_YEAR;

    fun getDisplayName(): String = when (this) {
        FINANCIAL_YEAR -> "Financial Year"
        CALENDAR_YEAR -> "Calendar Year"
    }
}
