package com.silverymusic.app.ui.screens.library

import com.silverymusic.app.data.DataError
import com.silverymusic.app.data.model.Artist
import com.silverymusic.app.data.model.Playlist

enum class LibraryTab(val label: String) {
    PLAYLISTS("Playlists"),
    ALBUMS("Albums"),
    ARTISTS("Artists"),
    RADIO("Radio"),
}

data class LibraryUiState(
    val profileName: String = "",
    /** True by default — the screen composes before init's first load runs. */
    val isLoading: Boolean = true,
    val error: DataError? = null,
    val likedCount: Int = 0,
    val selectedTab: LibraryTab = LibraryTab.PLAYLISTS,
    val playlists: List<Playlist> = emptyList(),
    val albums: List<Playlist> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val radio: List<Playlist> = emptyList(),
) {
    /** Only the tab in view decides whether the screen reads as empty. */
    val isSelectedTabEmpty: Boolean
        get() = when (selectedTab) {
            LibraryTab.PLAYLISTS -> playlists.isEmpty()
            LibraryTab.ALBUMS -> albums.isEmpty()
            LibraryTab.ARTISTS -> artists.isEmpty()
            LibraryTab.RADIO -> radio.isEmpty()
        }
}
