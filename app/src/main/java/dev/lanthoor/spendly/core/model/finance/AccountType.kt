package dev.lanthoor.spendly.core.model.finance

enum class AccountType {
    BANK,
    CARD,
    WALLET,
    CASH,
    LOAN,
    INVESTMENT;

    companion object {
        fun fromString(value: String): AccountType? = entries.find { it.name == value }

        fun fromStringOrDefault(value: String, default: AccountType = BANK): AccountType {
            return fromString(value) ?: default
        }
    }
}
