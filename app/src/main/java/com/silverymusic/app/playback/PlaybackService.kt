package com.silverymusic.app.playback

import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.silverymusic.app.R

/**
 * Hosts the app-owned [MediaSession] so a MediaStyle notification appears and
 * playback keeps running while the app is backgrounded. The session itself is
 * built and owned by [ExoPlaybackController]; this service just adopts it via
 * [PlaybackSessionHolder].
 *
 * Per the product decision, swiping the app away from Recents stops playback and
 * clears the notification (see [onTaskRemoved]).
 */
@OptIn(UnstableApi::class)
class PlaybackService : MediaSessionService() {

    override fun onCreate() {
        super.onCreate()
        // Brand the status-bar icon; the shade/lockscreen media controls stay
        // system-rendered from the session + artwork on Android 12+.
        setMediaNotificationProvider(
            DefaultMediaNotificationProvider.Builder(this).build().apply {
                setSmallIcon(R.drawable.ic_notif_small)
            },
        )
        // Adopt the session the controller published before it started us.
        // onGetSession only fires when a MediaController connects, and a plain
        // start intent never registers a session — without this the service
        // posts no notification and the system kills us for failing to call
        // startForeground() in time.
        adoptPublishedSession()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // A restart (or a start that races session creation) must re-adopt too.
        adoptPublishedSession()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        PlaybackSessionHolder.session

    /** Registers the shared session exactly once, which is what posts the notification. */
    private fun adoptPublishedSession() {
        val session = PlaybackSessionHolder.session ?: return
        if (sessions.none { it === session }) addSession(session)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        // Swiping the app away stops playback entirely. Media3's own helper pauses
        // every player and stops the service, which clears the notification and
        // routes us through onDestroy for the actual teardown.
        pauseAllPlayersAndStopSelf()
    }

    override fun onDestroy() {
        // Unregister before releasing: super.onDestroy() walks the session list,
        // and touching an already-released session there would throw.
        PlaybackSessionHolder.session?.let { removeSession(it) }
        PlaybackSessionHolder.releaseFromService()
        super.onDestroy()
    }
}
