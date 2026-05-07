package dev.lanthoor.spendly.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.lanthoor.spendly.domain.repository.PreferencesRepository
import dev.lanthoor.spendly.core.model.preferences.AppTheme
import dev.lanthoor.spendly.core.model.preferences.LockTimeout
import dev.lanthoor.spendly.core.model.preferences.YearType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    /**
     * Current theme preference, exposed as StateFlow for UI observation.
     * Caches the last value with WhileSubscribed(5000) for 5-second timeout.
     */
    val theme: StateFlow<AppTheme> = preferencesRepository.getTheme()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppTheme.SYSTEM
        )

    /**
     * SMS auto-detection enabled state.
     */
    val smsAutoDetectionEnabled: StateFlow<Boolean> = preferencesRepository
        .getSmsAutoDetectionEnabled()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    /**
     * Year type preference for financial calculations.
     */
    val yearType: StateFlow<YearType> = preferencesRepository.getYearType()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = YearType.CALENDAR
        )

    /**
     * App lock enabled state.
     */
    val appLockEnabled: StateFlow<Boolean> = preferencesRepository.getAppLockEnabled()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    /**
     * Lock timeout preference.
     */
    val lockTimeout: StateFlow<LockTimeout> = preferencesRepository.getLockTimeout()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LockTimeout.IMMEDIATELY
        )

    /**
     * Update theme preference.
     * @param newTheme The theme to set (LIGHT, DARK, or SYSTEM)
     */
    fun updateTheme(newTheme: AppTheme) {
        viewModelScope.launch {
            preferencesRepository.setTheme(newTheme)
        }
    }

    /**
     * Update year type preference.
     * @param newYearType The year type to set (CALENDAR or FINANCIAL)
     */
    fun updateYearType(newYearType: YearType) {
        viewModelScope.launch {
            preferencesRepository.setYearType(newYearType)
        }
    }

    /**
     * Update SMS auto-detection enabled state.
     * @param enabled Whether SMS auto-detection should be enabled
     */
    fun setSmsAutoDetectionEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setSmsAutoDetectionEnabled(enabled)
        }
    }

    /**
     * Update app lock enabled state.
     * @param enabled Whether app lock should be enabled
     */
    fun setAppLockEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setAppLockEnabled(enabled)
        }
    }

    /**
     * Update lock timeout preference.
     * @param timeout The lock timeout to set
     */
    fun setLockTimeout(timeout: LockTimeout) {
        viewModelScope.launch {
            preferencesRepository.setLockTimeout(timeout)
        }
    }
}
