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
     * All transactions screen - shows combined list of expenses and income
     */
    data object AllTransactions : Screen("transactions")

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
     * Income list screen - shows all income with filtering options
     */
    data object IncomeList : Screen("income")

    /**
     * Add new income screen - form for creating a new income
     */
    data object AddIncome : Screen("income/add")

    /**
     * Edit income screen - form for editing an existing income
     * @param incomeId The ID of the income to edit
     */
    data object EditIncome : Screen("income/edit/{incomeId}") {
        /**
         * Creates the route with the actual income ID
         */
        fun createRoute(incomeId: Long): String = "income/edit/$incomeId"

        /**
         * Argument key for the income ID
         */
        const val ARG_INCOME_ID = "incomeId"
    }

    /**
     * Analytics screen - charts and insights about spending patterns
     */
    data object Analytics : Screen("analytics")

    /**
     * Settings screen - app preferences and configuration
     */
    data object Settings : Screen("settings")

    /**
     * Account management screen - list all accounts
     */
    data object AccountList : Screen("accounts")

    /**
     * Edit account screen - form for editing an existing account
     * @param accountId The ID of the account to edit
     */
    data object EditAccount : Screen("accounts/edit/{accountId}") {
        /**
         * Creates the route with the actual account ID
         */
        fun createRoute(accountId: Long): String = "accounts/edit/$accountId"

        /**
         * Argument key for the account ID
         */
        const val ARG_ACCOUNT_ID = "accountId"
    }
}
