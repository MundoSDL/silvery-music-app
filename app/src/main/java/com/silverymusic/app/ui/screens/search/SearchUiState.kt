package com.silverymusic.app.ui.screens.search

import com.silverymusic.app.data.DataError
import com.silverymusic.app.data.model.Genre
import com.silverymusic.app.data.model.Track

data class SearchUiState(
    val profileName: String = "",
    val query: String = "",
    /** True by default — the screen composes before init's first search runs. */
    val isLoading: Boolean = true,
    val error: DataError? = null,
    val allGenres: List<Genre> = emptyList(),
    /** Non-null while a genre tile is filtering the results list. */
    val browsingGenre: Genre? = null,
    /** Trending when the query is blank, catalog matches otherwise. */
    val results: List<Track> = emptyList(),
) {
    val isSearching: Boolean get() = query.isNotBlank()

    val resultsTitle: String
        get() = when {
            browsingGenre != null -> browsingGenre.name
            isSearching -> "Results"
            else -> "Trending Now"
        }

    val filteredGenres: List<Genre>
        get() = if (query.isBlank()) allGenres else allGenres.filter { it.name.contains(query, ignoreCase = true) }

    val isEmpty: Boolean get() = results.isEmpty() && filteredGenres.isEmpty()

    val emptyMessage: String
        get() = if (isSearching) "Nothing matched \"$query\"." else "Nothing to browse yet."
}
