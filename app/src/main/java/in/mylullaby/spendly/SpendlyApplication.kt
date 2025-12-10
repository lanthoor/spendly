package `in`.mylullaby.spendly

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import `in`.mylullaby.spendly.di.ApplicationScope
import `in`.mylullaby.spendly.domain.repository.CategoryRepository
import javax.inject.Inject

/**
 * Application class for Spendly expense tracker.
 *
 * Annotated with @HiltAndroidApp to enable Hilt dependency injection.
 * Seeds predefined categories on first launch.
 */
@HiltAndroidApp
class SpendlyApplication : Application() {

    @Inject
    lateinit var categoryRepository: CategoryRepository

    @Inject
    @ApplicationScope
    lateinit var coroutineScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()

        // Seed predefined categories on first launch
        coroutineScope.launch {
            try {
                if (!categoryRepository.isPredefinedSeeded()) {
                    categoryRepository.seedPredefinedCategories()
                }
            } catch (e: Exception) {
                // Log error but don't crash the app
                // In production, this would use proper logging (Timber, etc.)
                e.printStackTrace()
            }
        }
    }
}
