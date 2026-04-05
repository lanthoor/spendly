package dev.lanthoor.spendly.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.lanthoor.spendly.domain.repository.PreferencesRepository
import dev.lanthoor.spendly.core.model.preferences.LockTimeout
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing app lock state and lifecycle.
 * Handles lock timeout logic and authentication state.
 */
@HiltViewModel
class AppLockViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    /**
     * Whether app lock is enabled in settings.
     */
    private val _isLockEnabled = MutableStateFlow<Boolean?>(null)
    val isLockEnabled: StateFlow<Boolean?> = _isLockEnabled

    /**
     * Current lock timeout setting.
     */
    private val _lockTimeout = MutableStateFlow(LockTimeout.IMMEDIATELY)
    val lockTimeout: StateFlow<LockTimeout> = _lockTimeout

    /**
     * Whether the app is currently locked.
     * Starts as true for security (locked by default if lock is enabled).
     */
    private val _shouldLock = MutableStateFlow(true)

    /**
     * Timestamp of when the app went to background (in milliseconds).
     */
    private val _lastBackgroundTime = MutableStateFlow(0L)

    init {
        // Load preferences eagerly
        viewModelScope.launch {
            preferencesRepository.getAppLockEnabled().collect { enabled ->
                _isLockEnabled.value = enabled
                // If lock is disabled, ensure we're unlocked
                if (!enabled) {
                    _shouldLock.value = false
                }
            }
        }

        viewModelScope.launch {
            preferencesRepository.getLockTimeout().collect { timeout ->
                _lockTimeout.value = timeout
            }
        }
    }

    /**
     * Combined flow that determines if the app should show the lock screen.
     * Only shows lock if: lock is enabled AND app is in locked state.
     * Shows lock by default (secure) while preference is loading (null).
     */
    val isLocked: StateFlow<Boolean> = combine(
        _isLockEnabled,
        _shouldLock
    ) { enabled, shouldLock ->
        // If preference not loaded yet (null), assume locked for security
        if (enabled == null) {
            true
        } else {
            enabled && shouldLock
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = true
    )

    /**
     * Called when app goes to background (onPause).
     * Records the current timestamp for timeout calculation.
     *
     * @param time Current time in milliseconds (from System.currentTimeMillis())
     */
    fun onBackground(time: Long) {
        val enabled = _isLockEnabled.value ?: false
        if (enabled) {
            _lastBackgroundTime.value = time
        }
    }

    /**
     * Called when app comes to foreground (onResume).
     * Checks if lock should be triggered based on timeout duration.
     *
     * @param time Current time in milliseconds (from System.currentTimeMillis())
     */
    fun onForeground(time: Long) {
        val enabled = _isLockEnabled.value

        // If preference not loaded yet, wait for it
        if (enabled == null) {
            return
        }

        if (!enabled) {
            // Lock not enabled, ensure unlocked
            _shouldLock.value = false
            return
        }

        val lastBackgroundTime = _lastBackgroundTime.value

        // Cold start (no previous background time recorded)
        if (lastBackgroundTime == 0L) {
            _shouldLock.value = true
            return
        }

        // Calculate time elapsed since background
        val elapsedTime = time - lastBackgroundTime
        val timeoutMillis = _lockTimeout.value.milliseconds

        // Lock if timeout exceeded
        _shouldLock.value = elapsedTime >= timeoutMillis
    }

    /**
     * Called when user successfully authenticates.
     * Unlocks the app.
     */
    fun unlock() {
        _shouldLock.value = false
    }
}
