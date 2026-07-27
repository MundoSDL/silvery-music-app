package com.silverymusic.app.playback

import android.content.Context
import android.os.Looper
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.silverymusic.app.data.model.ListeningStatus
import com.silverymusic.app.data.model.NowPlaying
import com.silverymusic.app.data.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Streams `Track.streamUrl` through ExoPlayer and projects the player's state
 * into [nowPlaying].
 *
 * ExoPlayer must be created and touched from the thread that built it, so every
 * mutation is funnelled through [onPlayerThread] onto the main dispatcher. The
 * player itself is created lazily on first playback — an app session that never
 * plays anything never allocates it.
 */
@OptIn(UnstableApi::class)
class ExoPlaybackController(
    context: Context,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
) : PlaybackController {

    private val appContext = context.applicationContext

    private val _queue = MutableStateFlow<List<Track>>(emptyList())
    override val queue: StateFlow<List<Track>> = _queue.asStateFlow()

    private val _nowPlaying = MutableStateFlow(NowPlaying(track = IDLE_TRACK, sourceLabel = ""))
    override val nowPlaying: StateFlow<NowPlaying> = _nowPlaying.asStateFlow()

    private var player: ExoPlayer? = null
    private var ticker: Job? = null

    /** Index-aligned with the player's media items; unplayable tracks are dropped. */
    private var playerTracks: List<Track> = emptyList()

    /** Likes are session-only in this demo — no database, matching the fake. */
    private val likedIds = mutableSetOf<String>()

    private val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) = syncFromPlayer()

        override fun onPlayerError(error: PlaybackException) {
            // A dead stream must not wedge the UI in a buffering state.
            _nowPlaying.update { it.copy(isPlaying = false, isBuffering = false) }
        }
    }

    override fun playQueue(tracks: List<Track>, startIndex: Int, sourceLabel: String) {
        if (tracks.isEmpty()) return
        val start = startIndex.coerceIn(tracks.indices)
        val selected = tracks[start].withLike()
        _queue.value = tracks.map { it.withLike() }
        _nowPlaying.update {
            it.copy(
                track = selected,
                sourceLabel = sourceLabel,
                positionMs = 0L,
                durationMs = selected.durationMs,
                isPlaying = false,
                isBuffering = true,
            )
        }

        onPlayerThread {
            val playable = tracks.filter { it.isPlayable }
            playerTracks = playable
            if (playable.isEmpty()) {
                // Metadata-only catalog (the offline sample data): keep the UI
                // consistent instead of handing the player an empty URI.
                _nowPlaying.update { it.copy(isBuffering = false) }
                return@onPlayerThread
            }
            val playerIndex = playable.indexOfFirst { it.id == selected.id }.coerceAtLeast(0)
            requirePlayer().run {
                setMediaItems(playable.map(::mediaItemFor), playerIndex, 0L)
                prepare()
                play()
            }
            startTicker()
        }
    }

    override fun primeQueue(tracks: List<Track>, sourceLabel: String) {
        if (tracks.isEmpty() || _queue.value.isNotEmpty()) return
        _queue.value = tracks
        val first = tracks.first()
        _nowPlaying.update {
            it.copy(track = first, sourceLabel = sourceLabel, positionMs = 0L, durationMs = first.durationMs)
        }
    }

    override fun togglePlayPause() = onPlayerThread {
        val active = player?.takeIf { it.mediaItemCount > 0 }
        if (active == null) {
            _nowPlaying.update { it.copy(isPlaying = !it.isPlaying) }
            return@onPlayerThread
        }
        if (active.isPlaying) active.pause() else active.play()
        startTicker()
    }

    override fun skipNext() = step(forward = true)

    override fun skipPrevious() = step(forward = false)

    private fun step(forward: Boolean) = onPlayerThread {
        val active = player?.takeIf { it.mediaItemCount > 0 }
        if (active == null) {
            stepStateOnly(if (forward) 1 else -1)
            return@onPlayerThread
        }
        // Repeat-all is set on the player, so these wrap at the queue edges.
        if (forward) active.seekToNextMediaItem() else active.seekToPreviousMediaItem()
        active.play()
        startTicker()
    }

    override fun seekTo(fraction: Float) = onPlayerThread {
        val clamped = fraction.coerceIn(0f, 1f)
        val active = player?.takeIf { it.mediaItemCount > 0 }
        val duration = active?.duration?.takeIf { it != C.TIME_UNSET && it > 0 }
        if (active != null && duration != null) {
            active.seekTo((duration * clamped).toLong())
            syncFromPlayer()
        } else {
            _nowPlaying.update { it.copy(positionMs = (it.durationMs * clamped).toLong()) }
        }
    }

    override fun toggleLike(trackId: String) {
        val liked = if (trackId in likedIds) {
            likedIds.remove(trackId); false
        } else {
            likedIds.add(trackId); true
        }
        _queue.update { tracks -> tracks.map { if (it.id == trackId) it.copy(isLiked = liked) else it } }
        _nowPlaying.update {
            if (it.track.id == trackId) it.copy(track = it.track.copy(isLiked = liked)) else it
        }
    }

    override fun startSync(friendName: String) =
        _nowPlaying.update { it.copy(listeningStatus = ListeningStatus.Synced(friendName)) }

    override fun endSync() = _nowPlaying.update { it.copy(listeningStatus = ListeningStatus.Solo) }

    override fun release() = onPlayerThread {
        ticker?.cancel()
        ticker = null
        player?.removeListener(listener)
        player?.release()
        player = null
        playerTracks = emptyList()
        scope.cancel()
    }

    private fun requirePlayer(): ExoPlayer = player ?: ExoPlayer.Builder(appContext)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .setUsage(C.USAGE_MEDIA)
                .build(),
            /* handleAudioFocus = */ true,
        )
        .setHandleAudioBecomingNoisy(true)
        .build()
        .also {
            it.repeatMode = Player.REPEAT_MODE_ALL
            it.addListener(listener)
            player = it
        }

    private fun syncFromPlayer() {
        val active = player ?: return
        val track = playerTracks.getOrNull(active.currentMediaItemIndex)?.withLike()
        val reportedDuration = active.duration.takeIf { it != C.TIME_UNSET && it > 0L }
        _nowPlaying.update { current ->
            current.copy(
                track = track ?: current.track,
                positionMs = active.currentPosition.coerceAtLeast(0L),
                durationMs = reportedDuration ?: track?.durationMs ?: current.durationMs,
                isPlaying = active.isPlaying,
                isBuffering = active.playbackState == Player.STATE_BUFFERING,
            )
        }
    }

    /** Player callbacks don't fire while position advances, so poll it. */
    private fun startTicker() {
        if (ticker?.isActive == true) return
        ticker = scope.launch {
            while (isActive) {
                delay(POSITION_TICK_MS)
                val active = player ?: break
                if (active.isPlaying || active.playbackState == Player.STATE_BUFFERING) syncFromPlayer()
            }
        }
    }

    private fun stepStateOnly(delta: Int) {
        val tracks = _queue.value
        if (tracks.isEmpty()) return
        val current = tracks.indexOfFirst { it.id == _nowPlaying.value.track.id }
        val next = tracks[((if (current < 0) 0 else current) + delta).mod(tracks.size)]
        _nowPlaying.update { it.copy(track = next, positionMs = 0L, durationMs = next.durationMs) }
    }

    private fun mediaItemFor(track: Track): MediaItem = MediaItem.Builder()
        .setMediaId(track.id)
        .setUri(track.streamUrl)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(track.title)
                .setArtist(track.artist)
                .setAlbumTitle(track.albumName)
                .setArtworkUri(track.artworkUrl?.toUri())
                .build(),
        )
        .build()

    private fun Track.withLike(): Track = copy(isLiked = id in likedIds)

    private inline fun onPlayerThread(crossinline block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) block() else scope.launch { block() }
    }

    private companion object {
        const val POSITION_TICK_MS = 500L

        /** Placeholder so the mini player has something to render before first play. */
        val IDLE_TRACK = Track(
            id = "",
            title = "Nothing playing",
            artist = "Pick a track to start",
            genre = "",
            durationMs = 0L,
        )
    }
}
