package com.silverymusic.app.ui.screens.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.silverymusic.app.data.MusicRepository
import com.silverymusic.app.data.model.ListeningStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SyncUiState(
    val listeningStatus: ListeningStatus = ListeningStatus.Solo,
    val recentListeners: List<String> = listOf("Elena", "Marcus", "Nadia Cole"),
)

class SyncViewModel(private val repository: MusicRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(SyncUiState())
    val uiState: StateFlow<SyncUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.nowPlaying.collect { nowPlaying ->
                _uiState.update { it.copy(listeningStatus = nowPlaying.listeningStatus) }
            }
        }
    }

    fun onStartSync(friendName: String) = repository.startSync(friendName)
    fun onEndSync() = repository.endSync()
}
