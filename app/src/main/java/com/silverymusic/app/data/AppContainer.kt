package com.silverymusic.app.data

import android.content.Context
import com.silverymusic.app.data.local.LikesStore
import com.silverymusic.app.data.local.ProfileStore
import com.silverymusic.app.data.local.SessionStore
import com.silverymusic.app.data.local.SilveryPrefs

/**
 * Hand-rolled DI. [init] is called once from the Application so the live
 * implementations can reach a Context (ExoPlayer, OkHttp cache, on-device
 * storage); until then the offline fake backs everything, which is also what
 * Compose previews get.
 */
object AppContainer {

    private var appContext: Context? = null

    /**
     * On-device stores. Created in [init] so the session, profiles and liked
     * songs are read back before the first screen composes; null-prefs fallbacks
     * keep previews and tests entirely in memory.
     */
    private var likesStore: LikesStore = LikesStore()
    private var profileStore: ProfileStore = ProfileStore()
    private var sessionStore: SessionStore = SessionStore(null)

    @Volatile
    private var musicRepositoryOverride: MusicRepository? = null

    @Volatile
    private var lyricsRepositoryOverride: LyricsRepository? = null

    /** Set when a live playback stack is installed; null on the offline fake. */
    @Volatile
    private var playbackRelease: (() -> Unit)? = null

    /**
     * Signing in renames the main profile; going guest marks it guest mode; signing
     * out returns profiles to factory state and drops every saved like.
     */
    val authRepository: AuthRepository by lazy {
        DemoAuthRepository(
            store = sessionStore,
            onSessionChanged = { state ->
                if (state == AuthState.SignedOut) likesStore.clearAll()
                profileStore.applySession(state)
            },
        )
    }

    private val fallbackMusicRepository: MusicRepository by lazy {
        FakeMusicRepository(profileStore = profileStore, likesStore = likesStore)
    }

    val musicRepository: MusicRepository
        get() = musicRepositoryOverride ?: fallbackMusicRepository

    val lyricsRepository: LyricsRepository
        get() = lyricsRepositoryOverride ?: EmptyLyricsRepository

    fun init(context: Context) {
        appContext = context.applicationContext
        val prefs = SilveryPrefs.from(context)
        sessionStore = SessionStore(prefs)
        likesStore = LikesStore(prefs)
        // Switching profile rebinds Liked Songs to that profile's collection.
        profileStore = ProfileStore(prefs) { profileId -> likesStore.bindProfile(profileId) }
        likesStore.bindProfile(profileStore.activeProfileId.value)
    }

    fun install(music: MusicRepository? = null, lyrics: LyricsRepository? = null) {
        music?.let { musicRepositoryOverride = it }
        lyrics?.let { lyricsRepositoryOverride = it }
    }

    /**
     * Wires the live Jamendo/ExoPlayer stack. Falls back to the offline fake when
     * no Jamendo client_id is configured, so the app still runs out of the box.
     */
    fun installLiveImplementations() {
        // Fully-qualified on purpose: this file's imports belong to the UI contract.
        val context = requireContext()
        val dispatcher = kotlinx.coroutines.Dispatchers.IO
        val clientId = com.silverymusic.app.BuildConfig.JAMENDO_CLIENT_ID

        val jamendo = clientId.takeIf { it.isNotBlank() }
            ?.let { com.silverymusic.app.data.network.NetworkFactory.jamendoService(it) }

        // Lyrics install unconditionally — the bundled tier demos with no key
        // and no network at all.
        val lyrics = com.silverymusic.app.data.repository.DefaultLyricsRepository(
            assets = context.assets,
            jamendo = jamendo,
            lrcLib = com.silverymusic.app.data.network.NetworkFactory.lrcLibService(),
            dispatcher = dispatcher,
        )

        // No client_id means no catalog to browse, so the offline fake stays in
        // place and the app still runs end to end.
        val music = jamendo?.let { service ->
            // One LikesStore across the controller and the local state, so the
            // player, Liked Songs and the notification all read the same list.
            val controller = com.silverymusic.app.playback.ExoPlaybackController(
                context = context,
                likesStore = likesStore,
            )
            playbackRelease = controller::release
            com.silverymusic.app.data.repository.JamendoMusicRepository(
                service = service,
                playback = controller,
                dispatcher = dispatcher,
                local = FakeMusicRepository(profileStore = profileStore, likesStore = likesStore),
            )
        }

        install(music = music, lyrics = lyrics)
    }

    /**
     * Frees the ExoPlayer instance. Called when the last Activity is genuinely
     * finishing — not on a configuration change, which would stop playback on
     * every rotation.
     */
    fun releasePlayback() {
        playbackRelease?.invoke()
    }

    fun requireContext(): Context =
        checkNotNull(appContext) { "AppContainer.init(context) must be called from Application.onCreate" }
}

private object EmptyLyricsRepository : LyricsRepository {
    override suspend fun lyricsFor(track: com.silverymusic.app.data.model.Track) =
        DataResult.Failure(DataError.NotConfigured)
}
