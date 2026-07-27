package com.silverymusic.app.playback

import com.silverymusic.app.data.model.NowPlaying
import com.silverymusic.app.data.model.Track
import kotlinx.coroutines.flow.StateFlow

/**
 * The playback half of [com.silverymusic.app.data.MusicRepository], split out so
 * the repository stays a catalog concern and the player can be swapped (real
 * ExoPlayer, or nothing at all in tests).
 */
interface PlaybackController {

    val nowPlaying: StateFlow<NowPlaying>
    val queue: StateFlow<List<Track>>

    fun playQueue(tracks: List<Track>, startIndex: Int = 0, sourceLabel: String)

    /**
     * Shows a queue in the mini player without starting it. Used once, on the
     * first catalog load, so the bar isn't empty before the user picks a track.
     */
    fun primeQueue(tracks: List<Track>, sourceLabel: String)

    fun togglePlayPause()
    fun skipNext()
    fun skipPrevious()
    fun seekTo(fraction: Float)
    fun toggleLike(trackId: String)
    fun startSync(friendName: String)
    fun endSync()

    fun release()
}
