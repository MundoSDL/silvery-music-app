package com.silverymusic.app.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.silverymusic.app.data.DataResult
import com.silverymusic.app.data.MusicRepository
import com.silverymusic.app.data.errorOrNull
import com.silverymusic.app.data.getOrNull
import com.silverymusic.app.data.model.Artist
import com.silverymusic.app.data.model.Playlist
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LibraryViewModel(private val repository: MusicRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        load()
        viewModelScope.launch {
            combine(repository.profiles, repository.activeProfileId) { profiles, activeId ->
                profiles.firstOrNull { it.id == activeId }?.name.orEmpty()
            }.collect { name -> _uiState.update { it.copy(profileName = name) } }
        }
        viewModelScope.launch {
            repository.likedTracks.collect { liked ->
                _uiState.update { it.copy(likedCount = liked.size) }
            }
        }
    }

    fun onRetry() = load()

    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val playlists = repository.libraryPlaylists()
            val albums = repository.libraryAlbums()
            val artists = repository.libraryArtists()
            val radio = repository.libraryRadio()

            val results: List<DataResult<*>> = listOf(playlists, albums, artists, radio)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = results.firstNotNullOfOrNull { result -> result.errorOrNull() },
                    playlists = playlists.getOrNull().orEmpty(),
                    albums = albums.getOrNull().orEmpty(),
                    artists = artists.getOrNull().orEmpty(),
                    radio = radio.getOrNull().orEmpty(),
                )
            }
        }
    }

    fun onPlayPlaylist(playlist: Playlist) {
        viewModelScope.launch {
            val tracks = repository.tracksFor(playlist).getOrNull().orEmpty()
            if (tracks.isNotEmpty()) repository.playQueue(tracks, 0, playlist.title)
        }
    }

    fun onPlayArtist(artist: Artist) {
        viewModelScope.launch {
            val tracks = repository.tracksForArtist(artist).getOrNull().orEmpty()
            if (tracks.isNotEmpty()) repository.playQueue(tracks, 0, artist.name)
        }
    }

    fun onTabSelected(tab: LibraryTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }
}
