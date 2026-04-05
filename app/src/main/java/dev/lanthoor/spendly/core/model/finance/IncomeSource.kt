package dev.lanthoor.spendly.core.model.finance

enum class IncomeSource {
    SALARY,
    FREELANCE,
    INVESTMENT,
    GIFTS,
    REFUND,
    BUSINESS,
    RENTAL,
    INTEREST,
    BONUS,
    OTHER;

    companion object {
        fun fromString(value: String): IncomeSource? = entries.find { it.name == value }

        fun fromStringOrDefault(value: String, default: IncomeSource = OTHER): IncomeSource {
            return fromString(value) ?: default
        }
    }
}
