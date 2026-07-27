package com.silverymusic.app.data.model

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val genre: String,
    val durationMs: Long,
    /** Playable MP3 URL. Null means metadata-only — the UI must not offer play. */
    val streamUrl: String? = null,
    val artworkUrl: String? = null,
    val albumName: String? = null,
    val isLiked: Boolean = false,
) {
    val durationLabel: String get() = formatDuration(durationMs)
    val isPlayable: Boolean get() = !streamUrl.isNullOrBlank()
}
