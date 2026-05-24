package dev.lanthoor.spendly

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ChartBar
import com.adamglin.phosphoricons.regular.ChartPieSlice
import com.adamglin.phosphoricons.regular.Gear
import com.adamglin.phosphoricons.regular.ListBullets
import com.adamglin.phosphoricons.regular.Plus
import dagger.hilt.android.AndroidEntryPoint
import dev.lanthoor.spendly.domain.model.IntegrityVerdict
import dev.lanthoor.spendly.domain.repository.InitializationState
import dev.lanthoor.spendly.ui.components.AddTransactionBottomSheet
import dev.lanthoor.spendly.ui.components.LockScreen
import dev.lanthoor.spendly.ui.navigation.Screen
import dev.lanthoor.spendly.ui.navigation.SpendlyNavHost
import dev.lanthoor.spendly.ui.screens.IntegrityBlockScreen
import dev.lanthoor.spendly.ui.screens.SplashScreen
import dev.lanthoor.spendly.ui.screens.settings.SettingsViewModel
import dev.lanthoor.spendly.ui.theme.SpendlyTheme
import dev.lanthoor.spendly.ui.viewmodels.AppLockViewModel
import dev.lanthoor.spendly.ui.viewmodels.InitializationViewModel
import dev.lanthoor.spendly.ui.viewmodels.IntegrityViewModel
import dev.lanthoor.spendly.utils.BiometricAuthManager

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val appLockViewModel: AppLockViewModel by viewModels()
    private val integrityViewModel: IntegrityViewModel by viewModels()
    private val initializationViewModel: InitializationViewModel by viewModels()

    // Notification permission launcher for Android 13+
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Permission result handled here
        // User can still use the app even if notification permission is denied
        // Notifications will just not be shown
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request notification permission on Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            val theme by settingsViewModel.theme.collectAsStateWithLifecycle()
            val integrityVerdict by integrityViewModel.verdict.collectAsStateWithLifecycle()
            val initializationState by initializationViewModel.initializationState.collectAsStateWithLifecycle()

            SpendlyTheme(theme = theme) {
                when (integrityVerdict) {
                    is IntegrityVerdict.Red -> {
                        IntegrityBlockScreen(
                            reason = (integrityVerdict as IntegrityVerdict.Red).reason,
                            onRetry = { integrityViewModel.retry() }
                        )
                    }

                    else -> {
                        when (initializationState) {
                            is InitializationState.Loading -> {
                                SplashScreen(
                                    initializationState = initializationState,
                                    onRetry = { initializationViewModel.retry() }
                                )
                            }

                            is InitializationState.Error -> {
                                SplashScreen(
                                    initializationState = initializationState,
                                    onRetry = { initializationViewModel.retry() }
                                )
                            }

                            is InitializationState.Success -> {
                                val isLocked by appLockViewModel.isLocked.collectAsStateWithLifecycle()

                                Box {
                                    SpendlyApp()

                                    if (isLocked) {
                                        LockScreen(
                                            onAuthenticationSuccess = { appLockViewModel.unlock() },
                                            biometricAuthManager = BiometricAuthManager(context = this@MainActivity)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        appLockViewModel.onForeground(System.currentTimeMillis())
    }

    override fun onPause() {
        super.onPause()
        appLockViewModel.onBackground(System.currentTimeMillis())
    }
}

@PreviewScreenSizes
@Composable
fun SpendlyApp() {
    val navController = rememberNavController()
    SpendlyApp(navController = navController)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpendlyApp(navController: NavHostController) {
    var showAddTransactionSheet by remember { mutableStateOf(false) }

    // Observe current back stack entry to sync bottom navigation
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Determine current destination based on route
    val currentDestination = when (currentRoute) {
        Screen.Dashboard.route -> AppDestinations.HOME
        Screen.AllTransactions.route -> AppDestinations.TRANSACTIONS
        Screen.Analytics.route -> AppDestinations.ANALYTICS
        Screen.Settings.route -> AppDestinations.SETTINGS
        // Map Settings sub-screens to Settings destination
        Screen.About.route -> AppDestinations.SETTINGS
        Screen.AccountList.route -> AppDestinations.SETTINGS
        Screen.BudgetList.route -> AppDestinations.SETTINGS
        Screen.RecurringTransactionList.route -> AppDestinations.SETTINGS
        else -> AppDestinations.HOME // Default to home for other sub-screens
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
                    selected = destination == currentDestination && destination != AppDestinations.ADD_TRANSACTION,
                    onClick = {
                        // Special handling for ADD_TRANSACTION - open modal instead of navigating
                        if (destination == AppDestinations.ADD_TRANSACTION) {
                            showAddTransactionSheet = true
                            return@item
                        }

                        // Navigate to the corresponding screen
                        val targetRoute = when (destination) {
                            AppDestinations.HOME -> Screen.Dashboard.route
                            AppDestinations.TRANSACTIONS -> Screen.AllTransactions.route
                            AppDestinations.ANALYTICS -> Screen.Analytics.route
                            AppDestinations.SETTINGS -> Screen.Settings.route
                            AppDestinations.ADD_TRANSACTION -> return@item // Already handled above
                        }

                        // Check if already on the target destination (including sub-screens)
                        val isAlreadyOnDestination = currentDestination == destination

                        if (!isAlreadyOnDestination) {
                            // Not on this destination at all, navigate normally
                            navController.navigate(targetRoute) {
                                // Pop up to start destination to avoid building large back stack
                                popUpTo(Screen.Dashboard.route) {
                                    saveState = true
                                }
                                // Avoid multiple copies of same destination
                                launchSingleTop = true
                                // Restore state when navigating back to a destination
                                // BUT: Settings should always start at root, not restore sub-screens
                                restoreState = destination != AppDestinations.SETTINGS
                            }
                        } else if (currentRoute != targetRoute) {
                            // Already on this destination but in a sub-screen
                            // Pop back to the base screen
                            navController.navigate(targetRoute) {
                                popUpTo(targetRoute) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }
                        // else: Already on the exact target route, do nothing
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

    // Add Transaction Modal Bottom Sheet
    if (showAddTransactionSheet) {
        AddTransactionBottomSheet(
            onDismiss = { showAddTransactionSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
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
    HOME("Dashboard", PhosphorIcons.Regular.ChartBar),
    TRANSACTIONS("Transactions", PhosphorIcons.Regular.ListBullets),
    ADD_TRANSACTION("Add", PhosphorIcons.Regular.Plus),
    ANALYTICS("Analytics", PhosphorIcons.Regular.ChartPieSlice),
    SETTINGS("Settings", PhosphorIcons.Regular.Gear),
}