package com.silverymusic.app.ui.screens.discoverycontrol

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.silverymusic.app.data.MusicRepository
import com.silverymusic.app.data.model.DiscoveryMode
import com.silverymusic.app.data.model.ListeningStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DiscoveryControlUiState(
    val selectedMode: DiscoveryMode = DiscoveryMode.BALANCED,
    val isSynced: Boolean = false,
)

class DiscoveryControlViewModel(private val repository: MusicRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(DiscoveryControlUiState())
    val uiState: StateFlow<DiscoveryControlUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(repository.discoveryMode, repository.nowPlaying) { mode, nowPlaying ->
                DiscoveryControlUiState(selectedMode = mode, isSynced = nowPlaying.listeningStatus is ListeningStatus.Synced)
            }.collect { state -> _uiState.update { state } }
        }
    }

    fun onModeSelected(mode: DiscoveryMode) {
        repository.setDiscoveryMode(mode)
    }
}
