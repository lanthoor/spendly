package `in`.mylullaby.spendly.domain.model

/**
 * Category type - distinguishes between expense and income categories
 */
enum class CategoryType {
    EXPENSE,
    INCOME
}

/**
 * Domain model representing an expense/income category.
 * Supports both predefined and custom categories.
 */
data class Category(
    val id: Long = 0,
    val name: String,
    val icon: String, // Material Icon name (e.g., "restaurant")
    val color: Int, // Android Color Int
    val isCustom: Boolean,
    val sortOrder: Int,
    val type: CategoryType = CategoryType.EXPENSE // Default to expense for backwards compatibility
) {
    companion object {
        /**
         * Predefined expense categories (13 total).
         * These categories are seeded on first app launch.
         */
        val PREDEFINED_EXPENSE = listOf(
            Category(1, "Food & Dining", "restaurant", 0xFFFF6B6B.toInt(), false, 1, CategoryType.EXPENSE),
            Category(2, "Travel", "flight", 0xFF4ECDC4.toInt(), false, 2, CategoryType.EXPENSE),
            Category(3, "Rent", "home", 0xFF95E1D3.toInt(), false, 3, CategoryType.EXPENSE),
            Category(4, "Utilities", "lightbulb", 0xFFFECA57.toInt(), false, 4, CategoryType.EXPENSE),
            Category(5, "Services", "build", 0xFF48DBFB.toInt(), false, 5, CategoryType.EXPENSE),
            Category(6, "Shopping", "shopping_cart", 0xFFFF9FF3.toInt(), false, 6, CategoryType.EXPENSE),
            Category(7, "Media", "movie", 0xFF54A0FF.toInt(), false, 7, CategoryType.EXPENSE),
            Category(8, "Healthcare", "local_hospital", 0xFFEE5A6F.toInt(), false, 8, CategoryType.EXPENSE),
            Category(9, "Gifts", "card_giftcard", 0xFFC44569.toInt(), false, 9, CategoryType.EXPENSE),
            Category(10, "Education", "school", 0xFF00D2D3.toInt(), false, 10, CategoryType.EXPENSE),
            Category(11, "Investments", "trending_up", 0xFF1DD1A1.toInt(), false, 11, CategoryType.EXPENSE),
            Category(12, "Groceries", "local_grocery_store", 0xFF10AC84.toInt(), false, 12, CategoryType.EXPENSE),
            Category(13, "Misc", "category", 0xFF9E9E9E.toInt(), false, 13, CategoryType.EXPENSE)
        )

        /**
         * Predefined income categories (10 total).
         * These categories are seeded on first app launch.
         */
        val PREDEFINED_INCOME = listOf(
            Category(101, "Salary", "briefcase", 0xFF2E7D32.toInt(), false, 1, CategoryType.INCOME),
            Category(102, "Freelance", "laptop", 0xFF00897B.toInt(), false, 2, CategoryType.INCOME),
            Category(103, "Business", "storefront", 0xFF1976D2.toInt(), false, 3, CategoryType.INCOME),
            Category(104, "Investments", "trending_up", 0xFF1DD1A1.toInt(), false, 4, CategoryType.INCOME),
            Category(105, "Rental", "home", 0xFF7B1FA2.toInt(), false, 5, CategoryType.INCOME),
            Category(106, "Interest", "bank", 0xFFE65100.toInt(), false, 6, CategoryType.INCOME),
            Category(107, "Gifts", "card_giftcard", 0xFFC44569.toInt(), false, 7, CategoryType.INCOME),
            Category(108, "Refund", "receipt", 0xFF00ACC1.toInt(), false, 8, CategoryType.INCOME),
            Category(109, "Bonus", "gift", 0xFFF57C00.toInt(), false, 9, CategoryType.INCOME),
            Category(110, "Other", "category", 0xFF9E9E9E.toInt(), false, 10, CategoryType.INCOME)
        )

        /**
         * All predefined categories (expense + income)
         */
        val PREDEFINED = PREDEFINED_EXPENSE + PREDEFINED_INCOME

        /**
         * Default "Misc" category ID for expense transactions without a specific category.
         */
        const val MISC_EXPENSE_CATEGORY_ID = 13L

        /**
         * Default "Other" category ID for income transactions without a specific category.
         */
        const val OTHER_INCOME_CATEGORY_ID = 110L

        @Deprecated("Use MISC_EXPENSE_CATEGORY_ID instead", ReplaceWith("MISC_EXPENSE_CATEGORY_ID"))
        const val MISC_CATEGORY_ID = MISC_EXPENSE_CATEGORY_ID
    }
}
