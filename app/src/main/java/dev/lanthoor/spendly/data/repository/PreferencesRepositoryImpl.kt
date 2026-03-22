package dev.lanthoor.spendly.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.lanthoor.spendly.domain.repository.PreferencesRepository
import dev.lanthoor.spendly.utils.AppLanguage
import dev.lanthoor.spendly.utils.AppTheme
import dev.lanthoor.spendly.utils.LockTimeout
import dev.lanthoor.spendly.utils.TimePeriod
import dev.lanthoor.spendly.utils.YearType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : PreferencesRepository {

    companion object {
        private val THEME_KEY = stringPreferencesKey("app_theme")
        private val LANGUAGE_KEY = stringPreferencesKey("app_language")
        private val SMS_AUTO_DETECTION_KEY = booleanPreferencesKey("sms_auto_detection")
        private val YEAR_TYPE_KEY = stringPreferencesKey("year_type")
        private val APP_LOCK_ENABLED_KEY = booleanPreferencesKey("app_lock_enabled")
        private val LOCK_TIMEOUT_KEY = stringPreferencesKey("lock_timeout")
        private val ANALYTICS_TIME_PERIOD_KEY = stringPreferencesKey("analytics_time_period")
    }

    override fun getTheme(): Flow<AppTheme> {
        return dataStore.data.map { preferences ->
            val themeName = preferences[THEME_KEY] ?: AppTheme.SYSTEM.name
            AppTheme.fromStringOrDefault(themeName, AppTheme.SYSTEM)
        }
    }

    override suspend fun setTheme(theme: AppTheme) {
        dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme.name
        }
    }

    override fun getLanguage(): Flow<AppLanguage> {
        return dataStore.data.map { preferences ->
            val languageName = preferences[LANGUAGE_KEY] ?: AppLanguage.ENGLISH.name
            AppLanguage.fromStringOrDefault(languageName, AppLanguage.ENGLISH)
        }
    }

    override suspend fun setLanguage(language: AppLanguage) {
        dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = language.name
        }
    }

    override fun getSmsAutoDetectionEnabled(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[SMS_AUTO_DETECTION_KEY] ?: false  // Default: disabled
        }
    }

    override suspend fun setSmsAutoDetectionEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[SMS_AUTO_DETECTION_KEY] = enabled
        }
    }

    override fun getYearType(): Flow<YearType> {
        return dataStore.data.map { preferences ->
            val typeName = preferences[YEAR_TYPE_KEY] ?: YearType.CALENDAR.name
            YearType.fromStringOrDefault(typeName, YearType.CALENDAR)
        }
    }

    override suspend fun setYearType(yearType: YearType) {
        dataStore.edit { preferences ->
            preferences[YEAR_TYPE_KEY] = yearType.name
        }
    }

    override fun getAppLockEnabled(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[APP_LOCK_ENABLED_KEY] ?: false  // Default: disabled
        }
    }

    override suspend fun setAppLockEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[APP_LOCK_ENABLED_KEY] = enabled
        }
    }

    override fun getLockTimeout(): Flow<LockTimeout> {
        return dataStore.data.map { preferences ->
            val timeoutName = preferences[LOCK_TIMEOUT_KEY] ?: LockTimeout.IMMEDIATELY.name
            LockTimeout.fromStringOrDefault(timeoutName, LockTimeout.IMMEDIATELY)
        }
    }

    override suspend fun setLockTimeout(timeout: LockTimeout) {
        dataStore.edit { preferences ->
            preferences[LOCK_TIMEOUT_KEY] = timeout.name
        }
    }

    override fun getAnalyticsTimePeriod(): Flow<TimePeriod> {
        return dataStore.data.map { preferences ->
            val periodString = preferences[ANALYTICS_TIME_PERIOD_KEY] ?: "THIS_MONTH"
            TimePeriod.fromStringOrDefault(periodString, TimePeriod.ThisMonth)
        }
    }

    override suspend fun setAnalyticsTimePeriod(period: TimePeriod) {
        dataStore.edit { preferences ->
            preferences[ANALYTICS_TIME_PERIOD_KEY] = period.toString()
        }
    }
}
