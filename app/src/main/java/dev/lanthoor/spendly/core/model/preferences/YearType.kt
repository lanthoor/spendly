package dev.lanthoor.spendly.core.model.preferences

enum class YearType {
    CALENDAR,
    FINANCIAL;

    companion object {
        fun fromString(value: String): YearType? = entries.find { it.name == value }

        fun fromStringOrDefault(value: String, default: YearType = CALENDAR): YearType {
            return fromString(value) ?: default
        }
    }

    fun getYearStart(selectedYear: Int, selectedMonth: Int): Pair<Int, Int> {
        return when (this) {
            CALENDAR -> Pair(selectedYear, 1)
            FINANCIAL -> if (selectedMonth >= 4) Pair(selectedYear, 4) else Pair(selectedYear - 1, 4)
        }
    }
}
