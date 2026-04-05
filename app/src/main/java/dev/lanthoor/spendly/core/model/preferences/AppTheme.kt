package dev.lanthoor.spendly.core.model.preferences

enum class AppTheme {
    LIGHT,
    DARK,
    SYSTEM;

    companion object {
        fun fromString(value: String): AppTheme? = entries.find { it.name == value }

        fun fromStringOrDefault(value: String, default: AppTheme = SYSTEM): AppTheme {
            return fromString(value) ?: default
        }
    }
}
