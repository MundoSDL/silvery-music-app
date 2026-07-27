package com.silverymusic.app.ui.screens.equalizer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.silverymusic.app.data.MusicRepository
import com.silverymusic.app.data.model.EqPreset
import com.silverymusic.app.data.model.EqSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EqualizerUiState(
    val settings: EqSettings = EqSettings(),
) {
    val isFlat: Boolean get() = settings.gains.all { it == 0f }
}

class EqualizerViewModel(private val repository: MusicRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(EqualizerUiState())
    val uiState: StateFlow<EqualizerUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.eqSettings.collect { settings ->
                _uiState.update { it.copy(settings = settings) }
            }
        }
    }

    fun onEnabledChange(enabled: Boolean) = repository.setEqEnabled(enabled)
    fun onPresetSelected(preset: EqPreset) = repository.setEqPreset(preset)
    fun onBandChange(index: Int, gainDb: Float) = repository.setEqBandGain(index, gainDb)
    fun onReset() = repository.resetEq()
}
