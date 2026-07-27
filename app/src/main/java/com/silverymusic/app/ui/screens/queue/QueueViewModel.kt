package com.silverymusic.app.ui.screens.queue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.silverymusic.app.data.MusicRepository
import com.silverymusic.app.data.model.NowPlaying
import com.silverymusic.app.data.model.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class QueueUiState(
    val nowPlaying: NowPlaying? = null,
    val queue: List<Track> = emptyList(),
) {
    /** Everything after the current track, in queue order. */
    val upNext: List<Track>
        get() {
            val currentId = nowPlaying?.track?.id ?: return queue
            val index = queue.indexOfFirst { it.id == currentId }
            return if (index >= 0) queue.drop(index + 1) else queue
        }

    val isEmpty: Boolean get() = upNext.isEmpty()
}

class QueueViewModel(private val repository: MusicRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(QueueUiState())
    val uiState: StateFlow<QueueUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // The live queue, not a re-fetch — reordering or a new source has to
            // show up here immediately.
            combine(repository.queue, repository.nowPlaying) { queue, nowPlaying ->
                queue to nowPlaying
            }.collect { (queue, nowPlaying) ->
                _uiState.update { it.copy(queue = queue, nowPlaying = nowPlaying) }
            }
        }
    }

    fun onPlayTrack(track: Track) {
        val queue = _uiState.value.queue
        val index = queue.indexOfFirst { it.id == track.id }
        if (index < 0) return
        repository.playQueue(queue, index, _uiState.value.nowPlaying?.sourceLabel ?: "Queue")
    }
}
