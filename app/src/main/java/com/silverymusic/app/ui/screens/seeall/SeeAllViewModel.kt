package com.silverymusic.app.ui.screens.seeall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.silverymusic.app.data.DataError
import com.silverymusic.app.data.MusicRepository
import com.silverymusic.app.data.errorOrNull
import com.silverymusic.app.data.getOrNull
import com.silverymusic.app.data.model.Artist
import com.silverymusic.app.data.model.Genre
import com.silverymusic.app.data.model.Playlist
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SeeAllUiState(
    val title: String = "",
    val playlists: List<Playlist> = emptyList(),
    val genres: List<Genre> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val isLoading: Boolean = false,
    val error: DataError? = null,
) {
    val isEmpty: Boolean get() = playlists.isEmpty() && genres.isEmpty() && artists.isEmpty()
}

/**
 * Backs the "See all" screen. Each [SeeAllSection] maps to the same repository
 * call the home/discover row already uses, so the full list is exactly the
 * source the preview was sliced from — no new backend surface.
 */
class SeeAllViewModel(
    private val repository: MusicRepository,
    private val section: SeeAllSection,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SeeAllUiState(title = section.title))
    val uiState: StateFlow<SeeAllUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        load()
    }

    fun onRetry() = load()

    fun onPlayPlaylist(playlist: Playlist) {
        viewModelScope.launch {
            val tracks = repository.tracksFor(playlist).getOrNull().orEmpty()
            if (tracks.isNotEmpty()) repository.playQueue(tracks, 0, playlist.title)
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

    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (section) {
                SeeAllSection.RECENTLY_PLAYED -> {
                    val result = repository.recentlyPlayed()
                    _uiState.update {
                        it.copy(isLoading = false, error = result.errorOrNull(), playlists = result.getOrNull().orEmpty())
                    }
                }
                SeeAllSection.MADE_FOR_YOU -> {
                    val result = repository.madeForYou()
                    _uiState.update {
                        it.copy(isLoading = false, error = result.errorOrNull(), playlists = result.getOrNull().orEmpty())
                    }
                }
                SeeAllSection.TOP_GENRES -> {
                    val result = repository.topGenres()
                    _uiState.update {
                        it.copy(isLoading = false, error = result.errorOrNull(), genres = result.getOrNull().orEmpty())
                    }
                }
                SeeAllSection.BROWSE_GENRES -> {
                    val result = repository.browseGenres()
                    _uiState.update {
                        it.copy(isLoading = false, error = result.errorOrNull(), genres = result.getOrNull().orEmpty())
                    }
                }
                SeeAllSection.YOUR_ARTISTS -> {
                    val result = repository.yourArtists()
                    _uiState.update {
                        it.copy(isLoading = false, error = result.errorOrNull(), artists = result.getOrNull().orEmpty())
                    }
                }
            }
        }
    }
}
