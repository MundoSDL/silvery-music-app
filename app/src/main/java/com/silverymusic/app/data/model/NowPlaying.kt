package com.silverymusic.app.data.model

/**
 * Position is held in milliseconds and the display strings are derived, so the
 * player, scrubber and lyrics all read one source of truth as playback advances.
 */
data class NowPlaying(
    val track: Track,
    val sourceLabel: String,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val listeningStatus: ListeningStatus = ListeningStatus.Solo,
) {
    val positionFraction: Float
        get() = if (durationMs > 0L) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f

    val elapsedLabel: String get() = formatDuration(positionMs)

    val remainingLabel: String get() = "-" + formatDuration((durationMs - positionMs).coerceAtLeast(0L))
}
