package com.silverymusic.app.playback

import android.content.Context
import android.content.Intent
import android.media.audiofx.Equalizer
import android.os.Bundle
import android.os.Looper
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.silverymusic.app.R
import com.silverymusic.app.data.local.LikesStore
import com.silverymusic.app.data.model.EqSettings
import com.silverymusic.app.data.model.ListeningStatus
import com.silverymusic.app.data.model.NowPlaying
import com.silverymusic.app.data.model.RepeatMode
import com.silverymusic.app.data.model.Track
import kotlin.math.ln
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
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
    /** Shared with the repository so likes are one persisted, per-profile list. */
    private val likesStore: LikesStore = LikesStore(),
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
) : PlaybackController {

    private val appContext = context.applicationContext

    private val _queue = MutableStateFlow<List<Track>>(emptyList())
    override val queue: StateFlow<List<Track>> = _queue.asStateFlow()

    private val _nowPlaying = MutableStateFlow(NowPlaying(track = IDLE_TRACK, sourceLabel = ""))
    override val nowPlaying: StateFlow<NowPlaying> = _nowPlaying.asStateFlow()

    override val likedTracks: StateFlow<List<Track>> get() = likesStore.likedTracks

    private val _repeatMode = MutableStateFlow(RepeatMode.ALL)
    override val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    private var player: ExoPlayer? = null
    private var ticker: Job? = null

    /** Media session wrapping [player]; created lazily alongside it. */
    private var mediaSession: MediaSession? = null

    /** Keeps the notification's custom buttons in step with like/repeat state. */
    private var customLayoutJob: Job? = null

    /** Guards against re-starting the foreground service on every queue change. */
    @Volatile
    private var serviceStarted = false

    private val likeCommand = SessionCommand(ACTION_LIKE, Bundle.EMPTY)
    private val repeatCommand = SessionCommand(ACTION_REPEAT, Bundle.EMPTY)

    init {
        // Liked state is per profile, so switching profiles changes which tracks
        // read as liked. Re-decorate whatever is on screen when the list changes.
        scope.launch {
            likesStore.likedTracks.collect {
                _queue.update { tracks -> tracks.map { track -> track.withLike() } }
                _nowPlaying.update { it.copy(track = it.track.withLike()) }
            }
        }
    }

    /** Index-aligned with the player's media items; unplayable tracks are dropped. */
    private var playerTracks: List<Track> = emptyList()

    // ---- Equalizer ---------------------------------------------------------

    private var equalizer: Equalizer? = null
    private var audioSessionId: Int = C.AUDIO_SESSION_ID_UNSET

    /** Last curve requested; re-applied whenever a fresh audio session appears. */
    private var pendingEq: EqSettings = EqSettings()

    private val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            syncFromPlayer()
            // Play/pause from the notification hits the player directly rather
            // than togglePlayPause(), so re-arm the position ticker here too.
            if (player.isPlaying && ticker?.isActive != true) startTicker()
        }

        override fun onPlayerError(error: PlaybackException) {
            // A dead stream must not wedge the UI in a buffering state.
            _nowPlaying.update { it.copy(isPlaying = false, isBuffering = false) }
        }
    }

    /**
     * The graphic EQ has to bind to the output's audio session id, which ExoPlayer
     * assigns lazily and can change across tracks — so the effect is (re)built here
     * each time a new session id is reported.
     */
    private val analyticsListener = object : AnalyticsListener {
        override fun onAudioSessionIdChanged(eventTime: AnalyticsListener.EventTime, audioSessionId: Int) {
            this@ExoPlaybackController.audioSessionId = audioSessionId
            setupEqualizer(audioSessionId)
        }
    }

    /**
     * Grants the two extra buttons (like, repeat) on top of the default player
     * commands and routes their taps back into the controller.
     */
    private val sessionCallback = object : MediaSession.Callback {
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
        ): MediaSession.ConnectionResult {
            val sessionCommands = MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon()
                .add(likeCommand)
                .add(repeatCommand)
                .build()
            return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                .setAvailableSessionCommands(sessionCommands)
                .build()
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle,
        ): ListenableFuture<SessionResult> {
            when (customCommand.customAction) {
                ACTION_LIKE -> player?.currentMediaItem?.mediaId?.let { toggleLike(it) }
                ACTION_REPEAT -> cycleRepeatMode()
            }
            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
        }
    }

    /** Two notification buttons whose icons reflect the current like/repeat state. */
    private fun buildCustomLayout(): List<CommandButton> {
        val likeButton = CommandButton.Builder()
            .setSessionCommand(likeCommand)
            .setIconResId(if (_nowPlaying.value.track.isLiked) R.drawable.ic_like_filled else R.drawable.ic_like)
            .setDisplayName(if (_nowPlaying.value.track.isLiked) "Unlike" else "Like")
            .setEnabled(true)
            .build()
        val repeatButton = CommandButton.Builder()
            .setSessionCommand(repeatCommand)
            .setIconResId(
                when (_repeatMode.value) {
                    RepeatMode.OFF -> R.drawable.ic_repeat_off
                    RepeatMode.ALL -> R.drawable.ic_repeat
                    RepeatMode.ONE -> R.drawable.ic_repeat_one
                },
            )
            .setDisplayName("Repeat")
            .setEnabled(true)
            .build()
        return listOf(likeButton, repeatButton)
    }

    private fun ensureServiceStarted() {
        if (serviceStarted) return
        // Background-start restrictions can reject this (Android 12+). The
        // notification is an enhancement, so a rejection must not take playback
        // down with it — retry on the next play instead.
        val started = runCatching {
            ContextCompat.startForegroundService(appContext, Intent(appContext, PlaybackService::class.java))
        }.isSuccess
        serviceStarted = started
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
            // The session/player now exist and are playing, so it's safe to
            // promote the hosting service to the foreground.
            ensureServiceStarted()
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
        val liked = likesStore.toggle(trackId) {
            // Grab the fullest copy we have of the track so the Liked Songs list
            // can render it without a re-fetch.
            _nowPlaying.value.track.takeIf { it.id == trackId }
                ?: _queue.value.firstOrNull { it.id == trackId }
        }
        _queue.update { tracks -> tracks.map { if (it.id == trackId) it.copy(isLiked = liked) else it } }
        _nowPlaying.update {
            if (it.track.id == trackId) it.copy(track = it.track.copy(isLiked = liked)) else it
        }
    }

    override fun cycleRepeatMode() {
        val next = _repeatMode.value.next()
        _repeatMode.value = next
        onPlayerThread { player?.repeatMode = next.toPlayerRepeatMode() }
    }

    override fun shuffleQueue() {
        val current = _nowPlaying.value.track.id
        onPlayerThread {
            val active = player?.takeIf { it.mediaItemCount > 1 }
            if (active != null) {
                // Only touch the tail after the current item, so the track that's
                // playing is never rebuilt or restarted.
                val currentIndex = active.currentMediaItemIndex
                val tailStart = currentIndex + 1
                if (tailStart < active.mediaItemCount) {
                    val shuffledTail = playerTracks.subList(tailStart, playerTracks.size).shuffled()
                    active.removeMediaItems(tailStart, active.mediaItemCount)
                    active.addMediaItems(shuffledTail.map(::mediaItemFor))
                    playerTracks = playerTracks.subList(0, tailStart) + shuffledTail
                }
                _queue.value = playerTracks.map { it.withLike() }
            } else {
                // No live player (offline/metadata-only): shuffle everything after
                // the current track in the visible queue.
                val tracks = _queue.value
                val index = tracks.indexOfFirst { it.id == current }
                if (index >= 0 && index < tracks.lastIndex) {
                    val head = tracks.subList(0, index + 1)
                    val tail = tracks.subList(index + 1, tracks.size).shuffled()
                    _queue.value = head + tail
                }
            }
        }
    }

    override fun applyEqualizer(settings: EqSettings) {
        pendingEq = settings
        onPlayerThread {
            equalizer?.let { eq -> runCatching { applyEqToEffect(eq, settings) } }
        }
    }

    override fun startSync(friendName: String) =
        _nowPlaying.update { it.copy(listeningStatus = ListeningStatus.Synced(friendName)) }

    override fun endSync() = _nowPlaying.update { it.copy(listeningStatus = ListeningStatus.Solo) }

    /**
     * Tears the player and session down. Deliberately leaves [scope] alive: the
     * process can outlive a swipe-away, and a cancelled scope would leave the
     * controller half-dead on the next launch (no position ticker, no notification
     * sync). Playing again rebuilds the player, session and collectors.
     */
    override fun release() = onPlayerThread {
        ticker?.cancel()
        ticker = null
        customLayoutJob?.cancel()
        customLayoutJob = null
        runCatching { equalizer?.release() }
        equalizer = null
        // Release the session before the player it wraps, then drop the shared handles.
        mediaSession?.release()
        mediaSession = null
        PlaybackSessionHolder.clear()
        serviceStarted = false
        player?.removeListener(listener)
        player?.removeAnalyticsListener(analyticsListener)
        player?.release()
        player = null
        playerTracks = emptyList()
        _nowPlaying.update { it.copy(isPlaying = false, isBuffering = false, positionMs = 0L) }
    }

    /**
     * Binds a hardware [Equalizer] to [sessionId] and pushes the current curve.
     * Wrapped defensively: some emulators expose no EQ effect and throw, in which
     * case audio simply plays unprocessed rather than crashing playback.
     */
    private fun setupEqualizer(sessionId: Int) {
        if (sessionId == C.AUDIO_SESSION_ID_UNSET) return
        runCatching {
            equalizer?.release()
            equalizer = Equalizer(0, sessionId).also { applyEqToEffect(it, pendingEq) }
        }.onFailure { equalizer = null }
    }

    private fun applyEqToEffect(eq: Equalizer, settings: EqSettings) {
        // setEnabled returns a status int, so it's a method call, not a property.
        eq.setEnabled(settings.enabled)
        if (!settings.enabled) return
        val range = eq.bandLevelRange // millibels: [min, max]
        val min = range[0].toInt()
        val max = range[1].toInt()
        for (band in 0 until eq.numberOfBands) {
            val centerHz = eq.getCenterFreq(band.toShort()) / 1000 // milliHz → Hz
            val gainDb = interpolateGainDb(settings.gains, centerHz)
            val millibels = (gainDb * 100f).toInt().coerceIn(min, max)
            eq.setBandLevel(band.toShort(), millibels.toShort())
        }
    }

    /**
     * The UI's seven bands rarely line up with the device's, so a hardware band's
     * gain is interpolated from the two nearest UI bands in log-frequency space —
     * which is how the ear spaces them too.
     */
    private fun interpolateGainDb(gains: List<Float>, freqHz: Int): Float {
        if (gains.isEmpty()) return 0f
        val f = freqHz.toFloat().coerceAtLeast(1f)
        if (f <= EQ_CENTERS_HZ.first()) return gains.first()
        if (f >= EQ_CENTERS_HZ.last()) return gains.last()
        for (i in 0 until EQ_CENTERS_HZ.lastIndex) {
            val lo = EQ_CENTERS_HZ[i]
            val hi = EQ_CENTERS_HZ[i + 1]
            if (f in lo..hi) {
                val t = (ln(f) - ln(lo)) / (ln(hi) - ln(lo))
                return gains.getOrElse(i) { 0f } + t * (gains.getOrElse(i + 1) { 0f } - gains.getOrElse(i) { 0f })
            }
        }
        return gains.last()
    }

    private fun RepeatMode.toPlayerRepeatMode(): Int = when (this) {
        RepeatMode.OFF -> Player.REPEAT_MODE_OFF
        RepeatMode.ALL -> Player.REPEAT_MODE_ALL
        RepeatMode.ONE -> Player.REPEAT_MODE_ONE
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
            it.repeatMode = _repeatMode.value.toPlayerRepeatMode()
            it.addListener(listener)
            it.addAnalyticsListener(analyticsListener)
            player = it

            // Wrap the same player in a session and publish it so PlaybackService
            // can surface the notification. Built before the service is started,
            // so onGetSession always finds it.
            val session = MediaSession.Builder(appContext, it)
                .setCallback(sessionCallback)
                .setCustomLayout(buildCustomLayout())
                .build()
            mediaSession = session
            PlaybackSessionHolder.publish(session, ::release)

            // Keep the notification's like/repeat icons in step with state.
            customLayoutJob?.cancel()
            customLayoutJob = scope.launch {
                combine(nowPlaying, repeatMode) { np, mode -> np.track.isLiked to mode }
                    .distinctUntilChanged()
                    .collect { mediaSession?.setCustomLayout(buildCustomLayout()) }
            }
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

    private fun Track.withLike(): Track = copy(isLiked = likesStore.isLiked(id))

    private inline fun onPlayerThread(crossinline block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) block() else scope.launch { block() }
    }

    private companion object {
        const val POSITION_TICK_MS = 500L

        /** Custom session-command actions for the two extra notification buttons. */
        const val ACTION_LIKE = "com.silverymusic.action.LIKE"
        const val ACTION_REPEAT = "com.silverymusic.action.REPEAT"

        /** Center frequencies of the UI's seven bands, aligned with EqSettings.BAND_LABELS. */
        val EQ_CENTERS_HZ = listOf(60f, 150f, 400f, 1000f, 2400f, 6000f, 15000f)

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
