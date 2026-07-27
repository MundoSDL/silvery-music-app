package com.silverymusic.app.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.silverymusic.app.data.DataResult
import com.silverymusic.app.data.MusicRepository
import com.silverymusic.app.data.errorOrNull
import com.silverymusic.app.data.getOrNull
import com.silverymusic.app.data.model.Genre
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchViewModel(private val repository: MusicRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val queryInput = MutableStateFlow("")
    private var genresJob: Job? = null
    private var genreBrowseJob: Job? = null

    init {
        loadGenres()
        observeQuery()
        viewModelScope.launch {
            combine(repository.profiles, repository.activeProfileId) { profiles, activeId ->
                profiles.firstOrNull { it.id == activeId }?.name.orEmpty()
            }.collect { name -> _uiState.update { it.copy(profileName = name) } }
        }
    }


    /**
     * Browsing a genre replaces the results list rather than starting playback —
     * Search is where you look around, not where you commit.
     */
    fun onGenreSelected(genre: Genre) {
        genreBrowseJob?.cancel()
        genreBrowseJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, browsingGenre = genre) }
            val result = repository.tracksForGenre(genre)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = result.errorOrNull(),
                    results = result.getOrNull().orEmpty(),
                )
            }
        }
    }

    fun onClearGenre() {
        genreBrowseJob?.cancel()
        _uiState.update { it.copy(browsingGenre = null) }
        genreBrowseJob = viewModelScope.launch { runSearch(_uiState.value.query) }
    }

    @OptIn(FlowPreview::class)
    private fun observeQuery() {
        viewModelScope.launch {
            // Every keystroke would otherwise be a catalog round trip.
            queryInput
                .debounce { if (it.isBlank()) 0L else 300L }
                .distinctUntilChanged()
                .collect { query -> runSearch(query) }
        }
    }

    private suspend fun runSearch(query: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        val result = repository.searchTracks(query)
        _uiState.update {
            it.copy(
                isLoading = false,
                error = result.errorOrNull(),
                results = result.getOrNull().orEmpty(),
            )
        }
    }

    private fun loadGenres() {
        genresJob?.cancel()
        genresJob = viewModelScope.launch {
            when (val result = repository.searchGenres()) {
                is DataResult.Success -> _uiState.update { it.copy(allGenres = result.data) }
                // The track search owns the error slot; genres only fill it in
                // when nothing else has already failed.
                is DataResult.Failure -> _uiState.update { it.copy(error = it.error ?: result.error) }
            }
        }
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
        queryInput.value = query
    }

    fun onRetry() {
        loadGenres()
        viewModelScope.launch { runSearch(_uiState.value.query) }
    }

    fun onPlayResult(index: Int) {
        val tracks = _uiState.value.results
        if (index !in tracks.indices) return
        repository.playQueue(tracks, index, _uiState.value.resultsTitle)
    }
}
