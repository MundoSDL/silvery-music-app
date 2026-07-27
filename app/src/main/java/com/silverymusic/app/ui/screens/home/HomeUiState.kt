package com.silverymusic.app.ui.screens.home

import com.silverymusic.app.data.DataError
import com.silverymusic.app.data.model.Artist
import com.silverymusic.app.data.model.Genre
import com.silverymusic.app.data.model.Playlist

data class HomeUiState(
    val profileName: String = "",
    // Starts true: the first load is kicked off in the ViewModel's init, and the
    // screen composes before that coroutine runs.
    val isLoading: Boolean = true,
    val error: DataError? = null,
    val recentlyPlayed: List<Playlist> = emptyList(),
    val madeForYou: List<Playlist> = emptyList(),
    val topGenres: List<Genre> = emptyList(),
    val yourArtists: List<Artist> = emptyList(),
) {
    val isEmpty: Boolean
        get() = recentlyPlayed.isEmpty() && madeForYou.isEmpty() &&
            topGenres.isEmpty() && yourArtists.isEmpty()
}
