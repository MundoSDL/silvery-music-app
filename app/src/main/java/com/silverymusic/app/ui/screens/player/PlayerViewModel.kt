package com.silverymusic.app.ui.screens.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.silverymusic.app.data.DataResult
import com.silverymusic.app.data.LyricsRepository
import com.silverymusic.app.data.MusicRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val repository: MusicRepository,
    private val lyricsRepository: LyricsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private val _effects = Channel<PlayerEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var lyricsJob: Job? = null

    init {
        viewModelScope.launch {
            repository.nowPlaying.collect { nowPlaying ->
                val trackChanged = _uiState.value.nowPlaying?.track?.id != nowPlaying.track.id
                _uiState.update { state ->
                    if (trackChanged) {
                        state.copy(
                            nowPlaying = nowPlaying,
                            lyrics = null,
                            lyricsError = null,
                            isLoadingLyrics = false,
                        )
                    } else {
                        state.copy(nowPlaying = nowPlaying)
                    }
                }
                // Refetch on a track change only while the pane is open; opening
                // it later fetches then, so a closed pane costs nothing.
                if (trackChanged && _uiState.value.showLyrics) loadLyrics()
            }
        }
    }

    fun onTogglePlayPause() = repository.togglePlayPause()
    fun onSkipNext() = repository.skipNext()
    fun onSkipPrevious() = repository.skipPrevious()
    fun onSeek(fraction: Float) = repository.seekTo(fraction)

    fun onToggleLike() {
        _uiState.value.nowPlaying?.let { repository.toggleLike(it.track.id) }
    }

    fun onToggleLyrics() {
        val showLyrics = !_uiState.value.showLyrics
        _uiState.update { it.copy(showLyrics = showLyrics) }
        if (showLyrics && _uiState.value.lyrics == null) loadLyrics()
    }

    fun onRetryLyrics() = loadLyrics()

    private fun loadLyrics() {
        val track = _uiState.value.nowPlaying?.track ?: return
        lyricsJob?.cancel()
        lyricsJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoadingLyrics = true, lyricsError = null) }
            when (val result = lyricsRepository.lyricsFor(track)) {
                is DataResult.Success -> _uiState.update {
                    it.copy(isLoadingLyrics = false, lyrics = result.data, lyricsError = null)
                }
                is DataResult.Failure -> _uiState.update {
                    it.copy(isLoadingLyrics = false, lyrics = null, lyricsError = result.error)
                }
            }
        }
    }

    fun onFeelingLucky() {
        repository.skipNext()
        _effects.trySend(PlayerEffect.ShowMessage("Jumping to something new"))
    }
}
