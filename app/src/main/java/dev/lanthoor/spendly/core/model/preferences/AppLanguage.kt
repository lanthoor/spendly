package dev.lanthoor.spendly.core.model.preferences

enum class AppLanguage(val code: String) {
    ENGLISH("en"),
    HINDI("hi"),
    MALAYALAM("ml");

    companion object {
        fun fromString(value: String): AppLanguage? = entries.find { it.name == value }

        fun fromStringOrDefault(value: String, default: AppLanguage = ENGLISH): AppLanguage {
            return fromString(value) ?: default
        }

        fun fromLocaleCode(code: String): AppLanguage? = entries.find { it.code == code }
    }
}
