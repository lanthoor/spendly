package dev.lanthoor.spendly.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.lanthoor.spendly.domain.repository.InitializationRepository
import dev.lanthoor.spendly.domain.repository.InitializationState
import dev.lanthoor.spendly.utils.RecurringTransactionProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing app initialization state.
 * Orchestrates the initialization flow and exposes state to the UI.
 *
 * Initialization starts automatically when the ViewModel is created.
 * After successful initialization, processes recurring transactions in background.
 */
@HiltViewModel
class InitializationViewModel @Inject constructor(
    private val initializationRepository: InitializationRepository,
    private val recurringTransactionProcessor: RecurringTransactionProcessor
) : ViewModel() {

    companion object {
        private const val TAG = "InitializationViewModel"
    }

    /**
     * Initialization state exposed to the UI.
     * Starts with Loading and transitions to Success or Error.
     *
     * Uses SharingStarted.Eagerly to begin initialization immediately
     * when the ViewModel is created.
     */
    val initializationState: StateFlow<InitializationState> = flow {
        // Start initialization
        initializationRepository.initialize().collect { state ->
            emit(state)

            // Once initialization succeeds, process recurring transactions in background
            if (state is InitializationState.Success) {
                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        recurringTransactionProcessor.processRecurringTransactions()
                    } catch (e: Exception) {
                        // Log error but don't fail initialization
                        // Recurring transactions are a background task that shouldn't block UI
                        Log.e(
                            TAG,
                            "Failed to process recurring transactions during initialization",
                            e
                        )
                    }
                }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly, // Start immediately
        initialValue = InitializationState.Loading
    )

    /**
     * Retry initialization if it failed.
     * Triggers a new initialization flow.
     */
    fun retry() {
        viewModelScope.launch {
            initializationRepository.initialize().collect {
                // StateFlow will be updated automatically through the main flow
            }
        }
    }
}
