package `in`.mylullaby.spendly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ChartPieSlice
import com.adamglin.phosphoricons.regular.Gear
import com.adamglin.phosphoricons.regular.House
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import `in`.mylullaby.spendly.ui.navigation.Screen
import `in`.mylullaby.spendly.ui.navigation.SpendlyNavHost
import `in`.mylullaby.spendly.ui.theme.SpendlyTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SpendlyTheme {
                SpendlyApp()
            }
        }
    }
}

@PreviewScreenSizes
@Composable
fun SpendlyApp() {
    val navController = rememberNavController()
    SpendlyApp(navController = navController)
}

@Composable
fun SpendlyApp(navController: NavHostController) {
    // Observe current back stack entry to sync bottom navigation
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Determine current destination based on route
    val currentDestination = when (currentRoute) {
        Screen.Dashboard.route -> AppDestinations.HOME
        Screen.Analytics.route -> AppDestinations.ANALYTICS
        Screen.Settings.route -> AppDestinations.SETTINGS
        else -> AppDestinations.HOME // Default to home for expense sub-screens
    }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach { destination ->
                item(
                    icon = {
                        Icon(
                            destination.icon,
                            contentDescription = destination.label
                        )
                    },
                    label = { Text(destination.label) },
                    selected = destination == currentDestination,
                    onClick = {
                        // Navigate to the corresponding screen
                        val route = when (destination) {
                            AppDestinations.HOME -> Screen.Dashboard.route
                            AppDestinations.ANALYTICS -> Screen.Analytics.route
                            AppDestinations.SETTINGS -> Screen.Settings.route
                        }

                        // Only navigate if not already on that destination
                        if (currentRoute != route) {
                            navController.navigate(route) {
                                // Pop up to start destination to avoid building large back stack
                                popUpTo(Screen.Dashboard.route) {
                                    saveState = true
                                }
                                // Avoid multiple copies of same destination
                                launchSingleTop = true
                                // Restore state when navigating back to a destination
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) {
        SpendlyNavHost(
            navController = navController,
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Primary navigation destinations in the app, shown in the bottom navigation bar/rail/drawer
 */
enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    HOME("Home", PhosphorIcons.Regular.House),
    ANALYTICS("Analytics", PhosphorIcons.Regular.ChartPieSlice),
    SETTINGS("Settings", PhosphorIcons.Regular.Gear),
}