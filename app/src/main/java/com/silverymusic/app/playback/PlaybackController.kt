package com.silverymusic.app.playback

import com.silverymusic.app.data.model.EqSettings
import com.silverymusic.app.data.model.NowPlaying
import com.silverymusic.app.data.model.RepeatMode
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

    /** Every track the user has hearted this session, newest first. */
    val likedTracks: StateFlow<List<Track>>

    /** Current repeat state; drives the player's own repeat mode. */
    val repeatMode: StateFlow<RepeatMode>

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

    /** Advances the repeat state Off → All → One → Off. */
    fun cycleRepeatMode()

    /** Randomises the not-yet-played tail of the queue without interrupting playback. */
    fun shuffleQueue()

    /** Applies the graphic-EQ curve to the live audio output. */
    fun applyEqualizer(settings: EqSettings)

    fun release()
}
