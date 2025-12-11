package `in`.mylullaby.spendly.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import `in`.mylullaby.spendly.ui.screens.dashboard.DashboardScreen
import `in`.mylullaby.spendly.ui.screens.expenses.AddExpenseScreen
import `in`.mylullaby.spendly.ui.screens.expenses.EditExpenseScreen
import `in`.mylullaby.spendly.ui.screens.expenses.ExpenseListScreen
import `in`.mylullaby.spendly.ui.screens.income.AddIncomeScreen
import `in`.mylullaby.spendly.ui.screens.income.EditIncomeScreen
import `in`.mylullaby.spendly.ui.screens.income.IncomeListScreen
import `in`.mylullaby.spendly.ui.screens.transactions.TransactionListScreen

/**
 * Main navigation host for the Spendly app.
 * Sets up all navigation routes and handles screen transitions.
 *
 * @param navController The navigation controller managing the back stack
 * @param modifier Optional modifier for the NavHost
 */
@Composable
fun SpendlyNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = modifier
    ) {
        // Dashboard (Home) Screen
        composable(Screen.Dashboard.route) {
            DashboardScreen()
        }

        // All Transactions Screen (combined expenses and income)
        composable(Screen.AllTransactions.route) {
            TransactionListScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Expense List Screen
        composable(Screen.ExpenseList.route) {
            ExpenseListScreen(
                onNavigateToAdd = {
                    navController.navigate(Screen.AddExpense.route)
                },
                onNavigateToEdit = { expenseId ->
                    navController.navigate(Screen.EditExpense.createRoute(expenseId))
                }
            )
        }

        // Add Expense Screen
        composable(Screen.AddExpense.route) {
            AddExpenseScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Edit Expense Screen with expense ID argument
        composable(
            route = Screen.EditExpense.route,
            arguments = listOf(
                navArgument(Screen.EditExpense.ARG_EXPENSE_ID) {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val expenseId = backStackEntry.arguments?.getLong(Screen.EditExpense.ARG_EXPENSE_ID) ?: 0L
            EditExpenseScreen(
                expenseId = expenseId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Income List Screen
        composable(Screen.IncomeList.route) {
            IncomeListScreen(
                onNavigateToAdd = {
                    navController.navigate(Screen.AddIncome.route)
                },
                onNavigateToEdit = { incomeId ->
                    navController.navigate(Screen.EditIncome.createRoute(incomeId))
                }
            )
        }

        // Add Income Screen
        composable(Screen.AddIncome.route) {
            AddIncomeScreen(
                onDismiss = {
                    navController.popBackStack()
                },
                onSuccess = {
                    navController.popBackStack()
                }
            )
        }

        // Edit Income Screen with income ID argument
        composable(
            route = Screen.EditIncome.route,
            arguments = listOf(
                navArgument(Screen.EditIncome.ARG_INCOME_ID) {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val incomeId = backStackEntry.arguments?.getLong(Screen.EditIncome.ARG_INCOME_ID) ?: 0L
            EditIncomeScreen(
                incomeId = incomeId,
                onDismiss = {
                    navController.popBackStack()
                },
                onSuccess = {
                    navController.popBackStack()
                },
                onDelete = {
                    navController.popBackStack()
                }
            )
        }

        // Analytics Screen
        composable(Screen.Analytics.route) {
            // Placeholder for Phase 6 - Analytics implementation
            PlaceholderScreen(text = "Analytics\nComing in Phase 6")
        }

        // Settings Screen
        composable(Screen.Settings.route) {
            // Placeholder for Phase 7 - Settings implementation
            PlaceholderScreen(text = "Settings\nComing in Phase 7")
        }
    }
}

/**
 * Temporary placeholder screen for routes not yet implemented
 */
@Composable
private fun PlaceholderScreen(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text)
    }
}
