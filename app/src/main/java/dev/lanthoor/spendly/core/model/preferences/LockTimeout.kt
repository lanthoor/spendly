package dev.lanthoor.spendly.core.model.preferences

enum class LockTimeout(val milliseconds: Long) {
    IMMEDIATELY(0L),
    ONE_MINUTE(60_000L),
    FIVE_MINUTES(300_000L),
    FIFTEEN_MINUTES(900_000L);

    companion object {
        fun fromString(value: String): LockTimeout? = entries.find { it.name == value }

        fun fromStringOrDefault(value: String, default: LockTimeout = IMMEDIATELY): LockTimeout {
            return fromString(value) ?: default
        }
    }
}
