package dev.lanthoor.spendly.core.model.finance

enum class RecurringFrequency {
    DAILY,
    WEEKLY,
    MONTHLY;

    companion object {
        fun fromString(value: String): RecurringFrequency? = entries.find { it.name == value }

        fun fromStringOrDefault(
            value: String,
            default: RecurringFrequency = MONTHLY,
        ): RecurringFrequency {
            return fromString(value) ?: default
        }
    }
}
