package dev.lanthoor.spendly.domain.repository

import dev.lanthoor.spendly.core.model.preferences.AppTheme
import dev.lanthoor.spendly.core.model.preferences.AiModelAvailability
import dev.lanthoor.spendly.core.model.preferences.AiEnrichmentSettings
import dev.lanthoor.spendly.core.model.preferences.LockTimeout
import dev.lanthoor.spendly.core.model.preferences.TimePeriod
import dev.lanthoor.spendly.core.model.preferences.YearType
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for app preferences stored in DataStore.
 * Handles theme settings and other user preferences.
 */
interface PreferencesRepository {
    /**
     * Get current theme preference as a Flow.
     * Returns SYSTEM as default if no preference is set.
     */
    fun getTheme(): Flow<AppTheme>

    /**
     * Update theme preference.
     * @param theme The theme to set (LIGHT, DARK, or SYSTEM)
     */
    suspend fun setTheme(theme: AppTheme)

    /**
     * Get SMS auto-detection enabled state.
     */
    fun getSmsAutoDetectionEnabled(): Flow<Boolean>

    /**
     * Set SMS auto-detection enabled state.
     */
    suspend fun setSmsAutoDetectionEnabled(enabled: Boolean)

    /**
     * Get year type preference as a Flow.
     * Returns CALENDAR as default if no preference is set.
     */
    fun getYearType(): Flow<YearType>

    /**
     * Update year type preference.
     * @param yearType The year type to set (CALENDAR or FINANCIAL)
     */
    suspend fun setYearType(yearType: YearType)

    /**
     * Get app lock enabled state.
     * Returns false as default if no preference is set.
     */
    fun getAppLockEnabled(): Flow<Boolean>

    /**
     * Set app lock enabled state.
     * @param enabled Whether app lock is enabled
     */
    suspend fun setAppLockEnabled(enabled: Boolean)

    /**
     * Get lock timeout preference as a Flow.
     * Returns IMMEDIATELY as default if no preference is set.
     */
    fun getLockTimeout(): Flow<LockTimeout>

    /**
     * Update lock timeout preference.
     * @param timeout The lock timeout to set
     */
    suspend fun setLockTimeout(timeout: LockTimeout)

    /**
     * Get analytics time period preference as a Flow.
     * Returns THIS_MONTH as default if no preference is set.
     */
    fun getAnalyticsTimePeriod(): Flow<TimePeriod>

    /**
     * Update analytics time period preference.
     * @param period The time period to set
     */
    suspend fun setAnalyticsTimePeriod(period: TimePeriod)

    fun getAiEnrichmentSettings(): Flow<AiEnrichmentSettings>

    suspend fun setAiEnrichmentEnabled(enabled: Boolean)

    suspend fun setAiModelAvailability(
        availability: AiModelAvailability,
        checkedAt: Long,
        baseModelName: String?,
        lastErrorCode: String?
    )

    suspend fun setAiEnrichmentBatchSize(batchSize: Int)

    suspend fun setAiPromptVersion(promptVersion: Int)
}
