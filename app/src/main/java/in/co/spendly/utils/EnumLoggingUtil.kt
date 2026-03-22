package `in`.co.spendly.utils

import android.util.Log

/**
 * Utility for consistent logging of enum deserialization issues.
 * Used when an unknown enum value is encountered and defaults are applied.
 */
object EnumLoggingUtil {
    private const val TAG = "EnumDeserialization"

    /**
     * Logs when an unknown enum value is encountered.
     * This typically indicates data integrity issues or version mismatches.
     *
     * @param enumName Name of the enum class
     * @param unknownValue The unknown value that was received
     * @param defaultValue The default value being used as fallback
     */
    fun logUnknownEnum(enumName: String, unknownValue: String, defaultValue: String) {
        Log.w(TAG, "Unknown $enumName value: '$unknownValue', using default: $defaultValue")
    }
}
