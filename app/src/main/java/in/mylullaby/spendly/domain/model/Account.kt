package `in`.mylullaby.spendly.domain.model

import `in`.mylullaby.spendly.utils.AccountType

/**
 * Domain model representing a financial account.
 * Supports both predefined and custom accounts.
 *
 * Accounts are used to track where transaction money comes from (income)
 * or goes to (expenses). Examples: "My Bank Account", "HDFC Credit Card", "Cash Wallet"
 */
data class Account(
    val id: Long = 0,
    val name: String,
    val type: AccountType,
    val icon: String, // Phosphor Icon name (e.g., "bank", "creditcard")
    val color: Int, // Android Color Int (ARGB)
    val isCustom: Boolean,
    val sortOrder: Int,
    val createdAt: Long,
    val modifiedAt: Long
) {
    companion object {
        /**
         * Predefined account seeded on first app launch.
         * This is the default account for all transactions.
         */
        val PREDEFINED = listOf(
            Account(
                id = 1,
                name = "My Account",
                type = AccountType.BANK,
                icon = "bank",
                color = 0xFF00BFA5.toInt(), // Material Teal A700
                isCustom = false,
                sortOrder = 1,
                createdAt = 0, // Will be set on insertion
                modifiedAt = 0  // Will be set on insertion
            )
        )

        /**
         * Default account ID for all transactions.
         * This account cannot be deleted.
         */
        const val DEFAULT_ACCOUNT_ID = 1L
    }
}
