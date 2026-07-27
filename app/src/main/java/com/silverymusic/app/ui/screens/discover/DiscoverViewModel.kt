package com.silverymusic.app.ui.screens.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.silverymusic.app.data.DataResult
import com.silverymusic.app.data.MusicRepository
import com.silverymusic.app.data.errorOrNull
import com.silverymusic.app.data.getOrNull
import com.silverymusic.app.data.model.Genre
import com.silverymusic.app.data.model.Artist
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val MIX_SOURCE_LABEL = "Discovery Mix"

class DiscoverViewModel(private val repository: MusicRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(DiscoverUiState())
    val uiState: StateFlow<DiscoverUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        load()
        viewModelScope.launch {
            combine(repository.profiles, repository.activeProfileId) { profiles, activeId ->
                profiles.firstOrNull { it.id == activeId }?.name.orEmpty()
            }.collect { name -> _uiState.update { it.copy(profileName = name) } }
        }
        viewModelScope.launch {
            repository.discoveryMode.collect { mode -> _uiState.update { it.copy(discoveryMode = mode) } }
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

    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val currentVibe = repository.currentVibe()
            val queueTracks = repository.discoverQueue()
            val browseGenres = repository.browseGenres()
            val yourArtists = repository.yourArtists()

            val results: List<DataResult<*>> = listOf(currentVibe, queueTracks, browseGenres, yourArtists)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = results.firstNotNullOfOrNull { result -> result.errorOrNull() },
                    currentVibe = currentVibe.getOrNull(),
                    queueTracks = queueTracks.getOrNull().orEmpty(),
                    browseGenres = browseGenres.getOrNull().orEmpty(),
                    yourArtists = yourArtists.getOrNull().orEmpty(),
                )
            }
        }
    }

    fun onPlayTrack(index: Int) {
        val tracks = _uiState.value.queueTracks
        if (index !in tracks.indices) return
        repository.playQueue(tracks, index, MIX_SOURCE_LABEL)
    }

    fun onPlayMix() {
        val tracks = _uiState.value.queueTracks
        if (tracks.isEmpty()) return
        repository.playQueue(tracks, 0, _uiState.value.currentVibe?.title ?: MIX_SOURCE_LABEL)
    }

    fun onShuffleMix() {
        val tracks = _uiState.value.queueTracks
        if (tracks.isEmpty()) return
        repository.playQueue(tracks.shuffled(), 0, _uiState.value.currentVibe?.title ?: MIX_SOURCE_LABEL)
    }

    fun onToggleLike(trackId: String) {
        repository.toggleLike(trackId)
        _uiState.update { state ->
            state.copy(
                queueTracks = state.queueTracks.map { track ->
                    if (track.id == trackId) track.copy(isLiked = !track.isLiked) else track
                },
            )
        }
    }
}
