package dev.lanthoor.spendly.core.model.finance

enum class TransactionType {
    EXPENSE,
    INCOME;

    companion object {
        fun fromString(value: String): TransactionType? = entries.find { it.name == value }

        fun fromStringOrDefault(
            value: String,
            default: TransactionType = EXPENSE,
        ): TransactionType {
            return fromString(value) ?: default
        }
    }
}
