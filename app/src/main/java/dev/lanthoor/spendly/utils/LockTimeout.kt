package dev.lanthoor.spendly.utils

/**
 * Lock timeout options for app lock feature.
 * Determines how long the app can be in background before requiring authentication.
 *
 * Types:
 * - IMMEDIATELY: Lock as soon as app goes to background (0ms)
 * - ONE_MINUTE: Lock after 1 minute of inactivity (60,000ms)
 * - FIVE_MINUTES: Lock after 5 minutes of inactivity (300,000ms)
 * - FIFTEEN_MINUTES: Lock after 15 minutes of inactivity (900,000ms)
 */
enum class LockTimeout(val milliseconds: Long) {
    IMMEDIATELY(0L),
    ONE_MINUTE(60_000L),
    FIVE_MINUTES(300_000L),
    FIFTEEN_MINUTES(900_000L);

    companion object {
        fun fromString(value: String): LockTimeout? {
            return entries.find { it.name == value }
        }

        /**
         * Safe parsing with fallback to default value.
         * Prevents IllegalArgumentException crashes from invalid DataStore values.
         * Logs warning when unknown value is encountered.
         */
        fun fromStringOrDefault(value: String, default: LockTimeout = IMMEDIATELY): LockTimeout {
            return fromString(value) ?: run {
                EnumLoggingUtil.logUnknownEnum("LockTimeout", value, default.name)
                default
            }
        }
    }
}

/**
 * Convert LockTimeout enum to display string.
 *
 * Examples:
 * - IMMEDIATELY -> "Immediately"
 * - ONE_MINUTE -> "After 1 minute"
 * - FIVE_MINUTES -> "After 5 minutes"
 * - FIFTEEN_MINUTES -> "After 15 minutes"
 */
fun LockTimeout.toDisplayName(): String {
    return when (this) {
        LockTimeout.IMMEDIATELY -> "Immediately"
        LockTimeout.ONE_MINUTE -> "After 1 minute"
        LockTimeout.FIVE_MINUTES -> "After 5 minutes"
        LockTimeout.FIFTEEN_MINUTES -> "After 15 minutes"
    }
}
