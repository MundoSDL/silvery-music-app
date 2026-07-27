package com.silverymusic.app.data.model

/** One lyric line. [startMs] is null when only unsynced plain text was available. */
data class LyricLine(
    val startMs: Long?,
    val text: String,
)

/**
 * Jamendo serves plain lyrics via `include=lyrics`; LRCLIB serves timestamped
 * LRC when it has a match. [isSynced] tells the player whether it can highlight
 * and auto-scroll, or must fall back to a static block.
 */
data class Lyrics(
    val trackId: String,
    val lines: List<LyricLine>,
    val isSynced: Boolean,
    val source: LyricsSource,
) {
    val isEmpty: Boolean get() = lines.isEmpty()

    /** Index of the line that should be highlighted at [positionMs], or -1. */
    fun activeLineIndex(positionMs: Long): Int {
        if (!isSynced) return -1
        return lines.indexOfLast { it.startMs != null && it.startMs <= positionMs }
    }
}

enum class LyricsSource { JAMENDO, LRCLIB, BUNDLED }
