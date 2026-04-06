package dev.lanthoor.spendly.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import dev.lanthoor.spendly.feature.dashboard.api.DashboardFeatureEntry
import dev.lanthoor.spendly.feature.expenses.api.AddExpenseFeatureEntry
import dev.lanthoor.spendly.feature.expenses.api.EditExpenseFeatureEntry
import dev.lanthoor.spendly.feature.income.api.AddIncomeFeatureEntry
import dev.lanthoor.spendly.feature.income.api.EditIncomeFeatureEntry
import dev.lanthoor.spendly.feature.transactions.api.TransactionsFeatureEntry
import dev.lanthoor.spendly.ui.screens.accounts.AccountListScreen
import dev.lanthoor.spendly.ui.screens.accounts.EditAccountScreen
import dev.lanthoor.spendly.ui.screens.analytics.AnalyticsScreen
import dev.lanthoor.spendly.ui.screens.datamanagement.DataManagementScreen
import dev.lanthoor.spendly.ui.screens.recurring.RecurringTransactionListScreen
import dev.lanthoor.spendly.ui.screens.settings.AboutScreen
import dev.lanthoor.spendly.ui.screens.settings.BudgetSettingsScreen
import dev.lanthoor.spendly.ui.screens.settings.LanguageSettingsScreen
import dev.lanthoor.spendly.ui.screens.settings.SettingsScreen

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
            DashboardFeatureEntry(
                onNavigateToBudgets = {
                    navController.navigate(Screen.BudgetList.route)
                }
            )
        }

        // All Transactions Screen (combined expenses and income)
        composable(Screen.AllTransactions.route) {
            TransactionsFeatureEntry(
                onNavigateBack = null  // Top-level destination, no back button
            )
        }

        // Add Expense Screen
        composable(Screen.AddExpense.route) { backStackEntry ->
            AddExpenseFeatureEntry(
                onNavigateBack = {
                    navController.popBackStack()
                },
                resetTrigger = backStackEntry
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
            val expenseId =
                backStackEntry.arguments?.getLong(Screen.EditExpense.ARG_EXPENSE_ID) ?: 0L
            EditExpenseFeatureEntry(
                expenseId = expenseId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Add Income Screen
        composable(Screen.AddIncome.route) { backStackEntry ->
            AddIncomeFeatureEntry(
                onDismiss = {
                    navController.popBackStack()
                },
                onSuccess = {
                    navController.popBackStack()
                },
                resetTrigger = backStackEntry
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
            EditIncomeFeatureEntry(
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
            AnalyticsScreen(
                onNavigateBack = { navController.navigateUp() }
            )
        }

        // Settings Screen
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateToAccounts = {
                    navController.navigate(Screen.AccountList.route)
                },
                onNavigateToBudgets = {
                    navController.navigate(Screen.BudgetList.route)
                },
                onNavigateToRecurringTransactions = {
                    navController.navigate(Screen.RecurringTransactionList.route)
                },
                onNavigateToAbout = {
                    navController.navigate(Screen.About.route)
                },
                onNavigateToDataManagement = {
                    navController.navigate(Screen.DataManagement.route)
                },
                onNavigateToLanguageSettings = {
                    navController.navigate(Screen.LanguageSettings.route)
                }
            )
        }

        // About Screen
        composable(Screen.About.route) {
            AboutScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Language Settings Screen
        composable(Screen.LanguageSettings.route) {
            LanguageSettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Account List Screen
        composable(Screen.AccountList.route) {
            AccountListScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToAddAccount = {
                    // Add handled via bottom sheet in AccountListScreen
                },
                onNavigateToEditAccount = { accountId ->
                    navController.navigate(Screen.EditAccount.createRoute(accountId))
                }
            )
        }

        // Edit Account Screen with account ID argument
        composable(
            route = Screen.EditAccount.route,
            arguments = listOf(
                navArgument(Screen.EditAccount.ARG_ACCOUNT_ID) {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val accountId =
                backStackEntry.arguments?.getLong(Screen.EditAccount.ARG_ACCOUNT_ID) ?: 0L
            EditAccountScreen(
                accountId = accountId,
                onDismiss = {
                    navController.popBackStack()
                }
            )
        }

        // Budget List Screen
        composable(Screen.BudgetList.route) {
            BudgetSettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Recurring Transaction List Screen (with modal bottom sheets for add/edit)
        composable(Screen.RecurringTransactionList.route) {
            RecurringTransactionListScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Data Management Screen (import/export)
        composable(Screen.DataManagement.route) {
            DataManagementScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
