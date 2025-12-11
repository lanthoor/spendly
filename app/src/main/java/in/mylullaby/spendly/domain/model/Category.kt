package `in`.mylullaby.spendly.domain.model

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
    val sortOrder: Int
) {
    companion object {
        /**
         * Predefined categories (13 total) as per PLAN.md.
         * These categories are seeded on first app launch.
         */
        val PREDEFINED = listOf(
            Category(1, "Food & Dining", "restaurant", 0xFFFF6B6B.toInt(), false, 1),
            Category(2, "Travel", "flight", 0xFF4ECDC4.toInt(), false, 2),
            Category(3, "Rent", "home", 0xFF95E1D3.toInt(), false, 3),
            Category(4, "Utilities", "lightbulb", 0xFFFECA57.toInt(), false, 4),
            Category(5, "Services", "build", 0xFF48DBFB.toInt(), false, 5),
            Category(6, "Shopping", "shopping_cart", 0xFFFF9FF3.toInt(), false, 6),
            Category(7, "Media", "movie", 0xFF54A0FF.toInt(), false, 7),
            Category(8, "Healthcare", "local_hospital", 0xFFEE5A6F.toInt(), false, 8),
            Category(9, "Gifts", "card_giftcard", 0xFFC44569.toInt(), false, 9),
            Category(10, "Education", "school", 0xFF00D2D3.toInt(), false, 10),
            Category(11, "Investments", "trending_up", 0xFF1DD1A1.toInt(), false, 11),
            Category(12, "Groceries", "local_grocery_store", 0xFF10AC84.toInt(), false, 12),
            Category(13, "Misc", "category", 0xFF9E9E9E.toInt(), false, 13)
        )

        /**
         * Default "Misc" category ID for transactions without a specific category.
         */
        const val MISC_CATEGORY_ID = 13L
    }
}
