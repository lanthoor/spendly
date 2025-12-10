package `in`.mylullaby.spendly.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing DataStore for app settings.
 * Settings keys to be used in ViewModel/Repository:
 * - theme: String ("light" / "dark" / "system")
 * - default_payment_method: String (enum value)
 * - notification_preferences: Boolean
 * - calendar_view_mode: String ("expenses" / "income" / "both")
 * - budget_alert_75_enabled: Boolean
 * - budget_alert_100_enabled: Boolean
 * - sms_auto_detection_enabled: Boolean
 */
@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    private const val SETTINGS_DATASTORE = "settings"

    /**
     * Provides DataStore<Preferences> for app settings.
     */
    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create {
            context.preferencesDataStoreFile(SETTINGS_DATASTORE)
        }
    }
}
