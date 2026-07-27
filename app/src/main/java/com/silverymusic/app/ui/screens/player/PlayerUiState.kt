package com.silverymusic.app.ui.screens.player

import com.silverymusic.app.data.DataError
import com.silverymusic.app.data.model.Lyrics
import com.silverymusic.app.data.model.NowPlaying
import com.silverymusic.app.data.model.RepeatMode

data class PlayerUiState(
    val nowPlaying: NowPlaying? = null,
    /** Whether the stage shows lyrics instead of the cover — same screen either way. */
    val showLyrics: Boolean = false,
    val lyrics: Lyrics? = null,
    val isLoadingLyrics: Boolean = false,
    val lyricsError: DataError? = null,
    val repeatMode: RepeatMode = RepeatMode.ALL,
) {
    val hasLyrics: Boolean get() = lyrics?.isEmpty == false

    /** Loaded, nothing failed, the provider simply had no words for this track. */
    val lyricsAreEmpty: Boolean get() = !isLoadingLyrics && lyricsError == null && !hasLyrics
}

sealed interface PlayerEffect {
    data class ShowMessage(val message: String) : PlayerEffect
}
