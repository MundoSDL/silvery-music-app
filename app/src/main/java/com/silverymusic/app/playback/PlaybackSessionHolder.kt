package com.silverymusic.app.playback

import androidx.media3.session.MediaSession

/**
 * Bridges the app-owned [MediaSession] to [PlaybackService].
 *
 * The player and session are created and owned by [ExoPlaybackController] (so the
 * whole in-app StateFlow projection keeps working unchanged); the service only
 * needs a handle to the session to surface it as a notification and a release
 * hook to tear playback down when the service goes away.
 */
object PlaybackSessionHolder {

    @Volatile
    var session: MediaSession? = null
        private set

    @Volatile
    private var release: (() -> Unit)? = null

    fun publish(session: MediaSession, release: () -> Unit) {
        this.session = session
        this.release = release
    }

    /** Clears the handles after the controller has released the player/session. */
    fun clear() {
        session = null
        release = null
    }

    /** Invoked by the service to release the player + session it is hosting. */
    fun releaseFromService() {
        release?.invoke()
    }
}
