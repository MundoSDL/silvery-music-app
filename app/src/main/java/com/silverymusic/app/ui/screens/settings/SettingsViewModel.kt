package com.silverymusic.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.silverymusic.app.data.MusicRepository
import com.silverymusic.app.data.model.AppSettings
import com.silverymusic.app.data.model.AudioQuality
import com.silverymusic.app.data.model.DiscoveryMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
    val discoveryMode: DiscoveryMode = DiscoveryMode.BALANCED,
    val activeProfileName: String = "",
    val showQualityPicker: Boolean = false,
)

class SettingsViewModel(private val repository: MusicRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.appSettings,
                repository.discoveryMode,
                repository.profiles,
                repository.activeProfileId,
            ) { settings, mode, profiles, activeId ->
                Triple(settings, mode, profiles.firstOrNull { it.id == activeId }?.name.orEmpty())
            }.collect { (settings, mode, name) ->
                _uiState.update { it.copy(settings = settings, discoveryMode = mode, activeProfileName = name) }
            }
        }
    }

    fun onQualityPickerVisibilityChange(visible: Boolean) =
        _uiState.update { it.copy(showQualityPicker = visible) }

    fun onQualitySelected(quality: AudioQuality) {
        repository.setAudioQuality(quality)
        _uiState.update { it.copy(showQualityPicker = false) }
    }

    fun onGaplessChange(enabled: Boolean) = repository.setGaplessPlayback(enabled)
    fun onNormalizationChange(enabled: Boolean) = repository.setVolumeNormalization(enabled)
    fun onAutoplayChange(enabled: Boolean) = repository.setAutoplaySimilar(enabled)
    fun onNotificationsChange(enabled: Boolean) = repository.setNotifications(enabled)
    fun onPrivateSessionChange(enabled: Boolean) = repository.setPrivateSession(enabled)
}
