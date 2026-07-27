package com.silverymusic.app.data

import com.silverymusic.app.data.model.Lyrics
import com.silverymusic.app.data.model.Track

/**
 * Resolution order is the implementation's concern, but the intent is:
 * LRCLIB (synced) → Jamendo `include=lyrics` (plain) → bundled LRC for the demo
 * tracks. The bundled tier exists so the feature still demonstrates when the
 * network or LRCLIB is unavailable.
 */
interface LyricsRepository {
    suspend fun lyricsFor(track: Track): DataResult<Lyrics>
}
