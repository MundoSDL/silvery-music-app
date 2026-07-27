package com.silverymusic.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.silverymusic.app.data.DataResult
import com.silverymusic.app.data.MusicRepository
import com.silverymusic.app.data.errorOrNull
import com.silverymusic.app.data.getOrNull
import com.silverymusic.app.data.model.Genre
import com.silverymusic.app.data.model.Artist
import com.silverymusic.app.data.model.Playlist
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: MusicRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        load()
        viewModelScope.launch {
            combine(repository.profiles, repository.activeProfileId) { profiles, activeId ->
                profiles.firstOrNull { it.id == activeId }?.name.orEmpty()
            }.collect { name -> _uiState.update { it.copy(profileName = name) } }
        }
    }


    fun onPlayGenre(genre: Genre) {
        viewModelScope.launch {
            val tracks = repository.tracksForGenre(genre).getOrNull().orEmpty()
            if (tracks.isNotEmpty()) repository.playQueue(tracks, 0, genre.name)
        }
    }

    fun onPlayArtist(artist: Artist) {
        viewModelScope.launch {
            val tracks = repository.tracksForArtist(artist).getOrNull().orEmpty()
            if (tracks.isNotEmpty()) repository.playQueue(tracks, 0, artist.name)
        }
    }

    fun onRetry() = load()

    fun onPlayPlaylist(playlist: Playlist) {
        viewModelScope.launch {
            val tracks = repository.tracksFor(playlist).getOrNull().orEmpty()
            if (tracks.isNotEmpty()) repository.playQueue(tracks, 0, playlist.title)
        }
    }

    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val recentlyPlayed = repository.recentlyPlayed()
            val madeForYou = repository.madeForYou()
            val topGenres = repository.topGenres()
            val yourArtists = repository.yourArtists()

            // Sections fail together far more often than individually (one key,
            // one connection), so the first failure speaks for the screen.
            val results: List<DataResult<*>> = listOf(recentlyPlayed, madeForYou, topGenres, yourArtists)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = results.firstNotNullOfOrNull { result -> result.errorOrNull() },
                    recentlyPlayed = recentlyPlayed.getOrNull().orEmpty(),
                    madeForYou = madeForYou.getOrNull().orEmpty(),
                    topGenres = topGenres.getOrNull().orEmpty(),
                    yourArtists = yourArtists.getOrNull().orEmpty(),
                )
            }
        }
    }
}
