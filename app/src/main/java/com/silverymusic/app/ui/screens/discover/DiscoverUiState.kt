package com.silverymusic.app.ui.screens.discover

import com.silverymusic.app.data.DataError
import com.silverymusic.app.data.model.Artist
import com.silverymusic.app.data.model.DiscoveryMode
import com.silverymusic.app.data.model.Genre
import com.silverymusic.app.data.model.Playlist
import com.silverymusic.app.data.model.Track

data class DiscoverUiState(
    val profileName: String = "",
    /** True by default — the screen composes before init's first load runs. */
    val isLoading: Boolean = true,
    val error: DataError? = null,
    val currentVibe: Playlist? = null,
    val discoveryMode: DiscoveryMode = DiscoveryMode.BALANCED,
    val queueTracks: List<Track> = emptyList(),
    val browseGenres: List<Genre> = emptyList(),
    val yourArtists: List<Artist> = emptyList(),
) {
    val isEmpty: Boolean
        get() = currentVibe == null && queueTracks.isEmpty() &&
            browseGenres.isEmpty() && yourArtists.isEmpty()

    /**
     * Only gates on having a queue at all. Whether a given track has a stream is
     * the repository's call — the offline fake plays without stream URLs.
     */
    val canPlayMix: Boolean get() = queueTracks.isNotEmpty()
}
