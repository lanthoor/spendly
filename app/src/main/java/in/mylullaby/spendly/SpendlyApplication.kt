package `in`.mylullaby.spendly

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Application class for Spendly expense tracker.
 *
 * Annotated with @HiltAndroidApp to enable Hilt dependency injection.
 * Initializes predefined categories on first launch.
 */
@HiltAndroidApp
class SpendlyApplication : Application() {

    // Application scope for coroutines
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Category repository will be injected once implemented
    // @Inject lateinit var categoryRepository: CategoryRepository

    override fun onCreate() {
        super.onCreate()

        // Initialize predefined categories on first launch
        // Will be uncommented once CategoryRepository is implemented
        /*
        applicationScope.launch {
            (categoryRepository as CategoryRepositoryImpl).initializePredefinedCategories()
        }
        */
    }
}
