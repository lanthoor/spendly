package `in`.co.spendly.domain.repository

import `in`.co.spendly.utils.AppLanguage
import `in`.co.spendly.utils.AppTheme
import `in`.co.spendly.utils.LockTimeout
import `in`.co.spendly.utils.TimePeriod
import `in`.co.spendly.utils.YearType
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
     * Get current language preference as a Flow.
     * Returns ENGLISH as default if no preference is set.
     */
    fun getLanguage(): Flow<AppLanguage>

    /**
     * Update language preference.
     * @param language The language to set (ENGLISH, HINDI, or MALAYALAM)
     */
    suspend fun setLanguage(language: AppLanguage)

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
}
