package com.silverymusic.app.ui.screens.liked

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.silverymusic.app.data.MusicRepository
import com.silverymusic.app.data.model.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LikedSongsUiState(
    val tracks: List<Track> = emptyList(),
) {
    val isEmpty: Boolean get() = tracks.isEmpty()
}

class LikedSongsViewModel(private val repository: MusicRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(LikedSongsUiState())
    val uiState: StateFlow<LikedSongsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.likedTracks.collect { tracks ->
                _uiState.update { it.copy(tracks = tracks) }
            }
        }
    }

    fun onPlayAll() {
        val tracks = _uiState.value.tracks
        if (tracks.isNotEmpty()) repository.playQueue(tracks, 0, "Liked Songs")
    }

    fun onPlayTrack(track: Track) {
        val tracks = _uiState.value.tracks
        val index = tracks.indexOfFirst { it.id == track.id }
        if (index >= 0) repository.playQueue(tracks, index, "Liked Songs")
    }

    fun onToggleLike(track: Track) = repository.toggleLike(track.id)
}
