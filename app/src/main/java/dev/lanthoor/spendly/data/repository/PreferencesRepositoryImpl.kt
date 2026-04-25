package dev.lanthoor.spendly.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.lanthoor.spendly.domain.repository.PreferencesRepository
import dev.lanthoor.spendly.core.model.preferences.AppLanguage
import dev.lanthoor.spendly.core.model.preferences.AppTheme
import dev.lanthoor.spendly.core.model.preferences.AiEnrichmentSettings
import dev.lanthoor.spendly.core.model.preferences.AiModelAvailability
import dev.lanthoor.spendly.core.model.preferences.AiPromptVersion
import dev.lanthoor.spendly.core.model.preferences.LockTimeout
import dev.lanthoor.spendly.core.model.preferences.TimePeriod
import dev.lanthoor.spendly.core.model.preferences.YearType
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
        private val AI_ENRICHMENT_ENABLED_KEY = booleanPreferencesKey("ai_enrichment_enabled")
        private val AI_MODEL_AVAILABILITY_KEY = stringPreferencesKey("ai_model_availability")
        private val AI_MODEL_BASE_NAME_KEY = stringPreferencesKey("ai_model_base_name")
        private val AI_MODEL_LAST_CHECKED_AT_KEY = stringPreferencesKey("ai_model_last_checked_at")
        private val AI_MODEL_LAST_ERROR_KEY = stringPreferencesKey("ai_model_last_error")
        private val AI_PROMPT_VERSION_KEY = stringPreferencesKey("ai_prompt_version")
        private val AI_BATCH_SIZE_KEY = stringPreferencesKey("ai_batch_size")

        private const val DEFAULT_AI_PROMPT_VERSION = AiPromptVersion.CURRENT
        private const val DEFAULT_AI_BATCH_SIZE = 20
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

    override fun getAiEnrichmentSettings(): Flow<AiEnrichmentSettings> {
        return dataStore.data.map { preferences ->
            val availabilityRaw = preferences[AI_MODEL_AVAILABILITY_KEY] ?: AiModelAvailability.UNKNOWN.name
            val availability = AiModelAvailability.entries.firstOrNull { it.name == availabilityRaw }
                ?: AiModelAvailability.UNKNOWN
            val promptVersion = preferences[AI_PROMPT_VERSION_KEY]?.toIntOrNull() ?: DEFAULT_AI_PROMPT_VERSION
            val batchSize = preferences[AI_BATCH_SIZE_KEY]?.toIntOrNull()
                ?.coerceIn(1, 100)
                ?: DEFAULT_AI_BATCH_SIZE
            val checkedAt = preferences[AI_MODEL_LAST_CHECKED_AT_KEY]?.toLongOrNull()

            AiEnrichmentSettings(
                enabled = preferences[AI_ENRICHMENT_ENABLED_KEY] ?: true,
                availability = availability,
                baseModelName = preferences[AI_MODEL_BASE_NAME_KEY],
                lastAvailabilityCheckAt = checkedAt,
                lastErrorCode = preferences[AI_MODEL_LAST_ERROR_KEY],
                promptVersion = promptVersion,
                batchSize = batchSize
            )
        }
    }

    override suspend fun setAiEnrichmentEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[AI_ENRICHMENT_ENABLED_KEY] = enabled
        }
    }

    override suspend fun setAiModelAvailability(
        availability: AiModelAvailability,
        checkedAt: Long,
        baseModelName: String?,
        lastErrorCode: String?
    ) {
        dataStore.edit { preferences ->
            preferences[AI_MODEL_AVAILABILITY_KEY] = availability.name
            preferences[AI_MODEL_LAST_CHECKED_AT_KEY] = checkedAt.toString()
            if (baseModelName.isNullOrBlank()) {
                preferences.remove(AI_MODEL_BASE_NAME_KEY)
            } else {
                preferences[AI_MODEL_BASE_NAME_KEY] = baseModelName
            }
            if (lastErrorCode.isNullOrBlank()) {
                preferences.remove(AI_MODEL_LAST_ERROR_KEY)
            } else {
                preferences[AI_MODEL_LAST_ERROR_KEY] = lastErrorCode
            }
        }
    }

    override suspend fun setAiEnrichmentBatchSize(batchSize: Int) {
        dataStore.edit { preferences ->
            preferences[AI_BATCH_SIZE_KEY] = batchSize.coerceIn(1, 100).toString()
        }
    }

    override suspend fun setAiPromptVersion(promptVersion: Int) {
        dataStore.edit { preferences ->
            preferences[AI_PROMPT_VERSION_KEY] = promptVersion.toString()
        }
    }
}
