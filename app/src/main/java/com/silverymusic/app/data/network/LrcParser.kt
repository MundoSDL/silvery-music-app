package com.silverymusic.app.data.network

import com.silverymusic.app.data.model.LyricLine

/** `[mm:ss.xx]`, `[mm:ss.xxx]` or `[mm:ss]`. A line may carry several. */
private val TIMESTAMP = Regex("""\[(\d{1,3}):(\d{1,2})(?:[.:](\d{1,3}))?]""")

/**
 * Parses an LRC body into timed lines. Metadata tags (`[ar:]`, `[ti:]`, …) and
 * empty lines are dropped; a line with several timestamps is emitted once per
 * timestamp so repeated choruses still highlight.
 */
internal fun parseLrc(body: String): List<LyricLine> {
    val lines = mutableListOf<LyricLine>()
    body.lineSequence().forEach { raw ->
        val matches = TIMESTAMP.findAll(raw).toList()
        if (matches.isEmpty()) return@forEach
        val text = raw.substring(matches.last().range.last + 1).trim()
        matches.forEach { match ->
            lines += LyricLine(startMs = match.toMillis(), text = text)
        }
    }
    return lines.sortedBy { it.startMs ?: 0L }
}

private fun MatchResult.toMillis(): Long {
    val minutes = groupValues[1].toLongOrNull() ?: 0L
    val seconds = groupValues[2].toLongOrNull() ?: 0L
    val fractionText = groupValues[3]
    // ".5" means 500ms, ".05" 50ms, ".005" 5ms — pad rather than assume hundredths.
    val fraction = when (fractionText.length) {
        0 -> 0L
        1 -> (fractionText.toLongOrNull() ?: 0L) * 100
        2 -> (fractionText.toLongOrNull() ?: 0L) * 10
        else -> fractionText.take(3).toLongOrNull() ?: 0L
    }
    return (minutes * 60 + seconds) * 1000 + fraction
}

/** Plain (unsynced) lyrics: one entry per non-blank line, no timestamps. */
internal fun parsePlainLyrics(body: String): List<LyricLine> =
    body.lineSequence()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .map { LyricLine(startMs = null, text = it) }
        .toList()
