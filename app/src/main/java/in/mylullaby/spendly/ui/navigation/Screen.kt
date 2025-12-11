package `in`.mylullaby.spendly.ui.navigation

/**
 * Defines all navigation routes in the app using a sealed class for type-safety.
 * Each screen has a unique route string used by Jetpack Compose Navigation.
 */
sealed class Screen(val route: String) {
    /**
     * Dashboard/Home screen - shows spending overview, recent transactions, and budget status
     */
    data object Dashboard : Screen("dashboard")

    /**
     * Expense list screen - shows all expenses with filtering options
     */
    data object ExpenseList : Screen("expenses")

    /**
     * Add new expense screen - form for creating a new expense
     */
    data object AddExpense : Screen("expenses/add")

    /**
     * Edit expense screen - form for editing an existing expense
     * @param expenseId The ID of the expense to edit
     */
    data object EditExpense : Screen("expenses/edit/{expenseId}") {
        /**
         * Creates the route with the actual expense ID
         */
        fun createRoute(expenseId: Long): String = "expenses/edit/$expenseId"

        /**
         * Argument key for the expense ID
         */
        const val ARG_EXPENSE_ID = "expenseId"
    }

    /**
     * Analytics screen - charts and insights about spending patterns
     */
    data object Analytics : Screen("analytics")

    /**
     * Settings screen - app preferences and configuration
     */
    data object Settings : Screen("settings")
}
