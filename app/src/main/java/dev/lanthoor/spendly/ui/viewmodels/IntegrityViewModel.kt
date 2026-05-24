package dev.lanthoor.spendly.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.lanthoor.spendly.domain.model.IntegrityVerdict
import dev.lanthoor.spendly.domain.repository.PlayIntegrityRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class IntegrityViewModel @Inject constructor(
    private val repository: PlayIntegrityRepository,
) : ViewModel() {

    private val _verdict = MutableStateFlow<IntegrityVerdict>(IntegrityVerdict.Unknown)
    val verdict: StateFlow<IntegrityVerdict> = _verdict.asStateFlow()

    init {
        checkIntegrity()
    }

    fun checkIntegrity() {
        viewModelScope.launch {
            _verdict.value = try {
                withContext(Dispatchers.IO) {
                    repository.checkIntegrity()
                }
            } catch (e: Exception) {
                IntegrityVerdict.Red(e.message ?: "Integrity check failed")
            }
        }
    }

    fun retry() {
        _verdict.value = IntegrityVerdict.Unknown
        checkIntegrity()
    }
}
