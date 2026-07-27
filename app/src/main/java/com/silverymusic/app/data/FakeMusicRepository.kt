package com.silverymusic.app.data

import com.silverymusic.app.data.model.AppSettings
import com.silverymusic.app.data.model.Artist
import com.silverymusic.app.data.model.AudioQuality
import com.silverymusic.app.data.model.DiscoveryMode
import com.silverymusic.app.data.model.EqPreset
import com.silverymusic.app.data.model.EqSettings
import com.silverymusic.app.data.model.Genre
import com.silverymusic.app.data.model.ListeningStatus
import com.silverymusic.app.data.model.NowPlaying
import com.silverymusic.app.data.model.Playlist
import com.silverymusic.app.data.model.Profile
import com.silverymusic.app.data.model.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Offline sample data transcribed from the Figma catalog copy. Kept alongside
 * the live Jamendo implementation so Compose previews, unit tests and a
 * no-network demo all still work.
 */
class FakeMusicRepository : MusicRepository {

    private val glassCeiling = Track("t1", "Glass Ceiling", "The Hollow", "Indie", durationMs = 251_000)
    private val neonUndertow = Track("t2", "Neon Undertow", "Mira Lane", "Ambient", durationMs = 222_000)
    private val signalLost = Track("t3", "Signal Lost", "Faded Echo", "Electronic", durationMs = 238_000)

    private val sampleTracks = listOf(neonUndertow, glassCeiling, signalLost)

    override suspend fun recentlyPlayed() = DataResult.Success(
        listOf(
            Playlist("p1", "Late Night Drift", "Ambient", trackCount = 24, durationLabel = "1 hr 32 min"),
            Playlist("p2", "Chill Mornings", "Lo-fi", trackCount = 16, durationLabel = "58 min"),
            Playlist("p3", "Focus Mode", "Electronic", trackCount = 8, durationLabel = "8 tracks"),
        ),
    )

    override suspend fun madeForYou() = DataResult.Success(
        listOf(
            Playlist("p4", "Deep Focus", "12 tracks", trackCount = 12),
            Playlist("p5", "Night Drive", "8 tracks", trackCount = 8),
        ),
    )

    override suspend fun topGenres() = DataResult.Success(
        listOf(Genre("g1", "Ambient"), Genre("g2", "Electronic"), Genre("g3", "Lo-fi"), Genre("g4", "Indie")),
    )

    override suspend fun yourArtists() = DataResult.Success(
        listOf(
            Artist("a1", "Mira Lane"), Artist("a2", "The Hollow"), Artist("a3", "Faded Echo"),
            Artist("a4", "Nadia Cole"), Artist("a5", "Bloom Atlas"),
        ),
    )

    override suspend fun currentVibe() = DataResult.Success(
        Playlist("vibe", "Late Night Drift", "Ambient · Indie · Electronic", trackCount = 0),
    )

    override suspend fun discoverQueue() = DataResult.Success(sampleTracks)

    override suspend fun browseGenres() = DataResult.Success(
        listOf(
            Genre("g1", "Ambient"), Genre("g5", "Indie"), Genre("g2", "Electronic"),
            Genre("g3", "Lo-fi"), Genre("g6", "Jazz"),
        ),
    )

    override suspend fun libraryPlaylists() = DataResult.Success(
        listOf(
            Playlist("p1", "Late Night Drift", "24 songs · 1 hr 32 min", 24),
            Playlist("p6", "Morning Coffee", "16 songs · 58 min", 16),
            Playlist("p4", "Deep Focus", "31 songs · 2 hr 4 min", 31),
            Playlist("p5", "Night Drive", "18 songs · 1 hr 12 min", 18),
            Playlist("p7", "Chill Sunday", "22 songs · 1 hr 21 min", 22),
            Playlist("p8", "Workout Beats", "28 songs · 1 hr 45 min", 28),
            Playlist("p9", "Rainy Afternoon", "15 songs · 52 min", 15),
        ),
    )

    override suspend fun libraryAlbums() = DataResult.Success(
        listOf(
            Playlist("al1", "Glass Ceiling", "The Hollow", trackCount = 11),
            Playlist("al2", "Neon Undertow", "Mira Lane", trackCount = 9),
        ),
    )

    override suspend fun libraryArtists() = yourArtists()

    override suspend fun libraryRadio() = DataResult.Success(
        listOf(
            Playlist("r1", "Ambient Radio", "Endless ambient mix", trackCount = 0),
            Playlist("r2", "Indie Radio", "Endless indie mix", trackCount = 0),
        ),
    )

    override suspend fun searchGenres() = DataResult.Success(
        listOf(
            Genre("g1", "Ambient"), Genre("g5", "Indie"), Genre("g2", "Electronic"), Genre("g3", "Lo-fi"),
            Genre("g6", "Jazz"), Genre("g7", "Hip-Hop"), Genre("g8", "Classical"), Genre("g9", "Pop"),
        ),
    )

    override suspend fun trendingNow() = DataResult.Success(sampleTracks)

    override suspend fun searchTracks(query: String) = DataResult.Success(
        if (query.isBlank()) {
            sampleTracks
        } else {
            sampleTracks.filter {
                it.title.contains(query, ignoreCase = true) || it.artist.contains(query, ignoreCase = true)
            }
        },
    )

    /** The offline catalog has no per-row track lists; every row plays the sample set. */
    override suspend fun tracksFor(playlist: Playlist) = DataResult.Success(sampleTracks)

    override suspend fun tracksForGenre(genre: Genre) = DataResult.Success(
        sampleTracks.filter { it.genre.equals(genre.name, ignoreCase = true) }.ifEmpty { sampleTracks },
    )

    override suspend fun tracksForArtist(artist: Artist) = DataResult.Success(
        sampleTracks.filter { it.artist.equals(artist.name, ignoreCase = true) }.ifEmpty { sampleTracks },
    )

    private val _profiles = MutableStateFlow(
        listOf(
            Profile("u1", "John Doe", "Main profile", isKid = false, accentIndex = 0, isRemovable = false),
            Profile("u2", "Emma", "Kid profile · filtered content", isKid = true, accentIndex = 3),
        ),
    )
    override val profiles: StateFlow<List<Profile>> = _profiles.asStateFlow()

    private val _activeProfileId = MutableStateFlow("u1")
    override val activeProfileId: StateFlow<String> = _activeProfileId.asStateFlow()
    override fun selectProfile(profileId: String) {
        _activeProfileId.value = profileId
    }

    private var nextProfileId = 3

    override fun addProfile(name: String, isKid: Boolean, accentIndex: Int) {
        _profiles.update {
            it + Profile(
                id = "u${nextProfileId++}",
                name = name,
                subtitle = if (isKid) "Kid profile · filtered content" else "Standard profile",
                isKid = isKid,
                accentIndex = accentIndex,
            )
        }
    }

    override fun renameProfile(profileId: String, name: String) {
        _profiles.update { profiles -> profiles.map { if (it.id == profileId) it.copy(name = name) else it } }
    }

    override fun removeProfile(profileId: String) {
        _profiles.update { profiles -> profiles.filterNot { it.id == profileId && it.isRemovable } }
        if (_activeProfileId.value == profileId) {
            _profiles.value.firstOrNull()?.let { _activeProfileId.value = it.id }
        }
    }

    private val _discoveryMode = MutableStateFlow(DiscoveryMode.BALANCED)
    override val discoveryMode: StateFlow<DiscoveryMode> = _discoveryMode.asStateFlow()
    override fun setDiscoveryMode(mode: DiscoveryMode) {
        _discoveryMode.value = mode
    }

    private val _eqSettings = MutableStateFlow(EqSettings())
    override val eqSettings: StateFlow<EqSettings> = _eqSettings.asStateFlow()

    override fun setEqEnabled(enabled: Boolean) = _eqSettings.update { it.copy(enabled = enabled) }

    override fun setEqPreset(preset: EqPreset) =
        _eqSettings.update { it.copy(preset = preset, gains = EqSettings.gainsFor(preset)) }

    override fun setEqBandGain(bandIndex: Int, gainDb: Float) {
        _eqSettings.update { settings ->
            val gains = settings.gains.toMutableList()
            gains[bandIndex] = gainDb.coerceIn(EqSettings.MIN_GAIN_DB, EqSettings.MAX_GAIN_DB)
            settings.copy(preset = EqPreset.CUSTOM, gains = gains)
        }
    }

    override fun resetEq() =
        _eqSettings.update { it.copy(preset = EqPreset.FLAT, gains = EqSettings.gainsFor(EqPreset.FLAT)) }

    private val _appSettings = MutableStateFlow(AppSettings())
    override val appSettings: StateFlow<AppSettings> = _appSettings.asStateFlow()

    override fun setAudioQuality(quality: AudioQuality) = _appSettings.update { it.copy(audioQuality = quality) }
    override fun setGaplessPlayback(enabled: Boolean) = _appSettings.update { it.copy(gaplessPlayback = enabled) }
    override fun setVolumeNormalization(enabled: Boolean) = _appSettings.update { it.copy(volumeNormalization = enabled) }
    override fun setAutoplaySimilar(enabled: Boolean) = _appSettings.update { it.copy(autoplaySimilar = enabled) }
    override fun setNotifications(enabled: Boolean) = _appSettings.update { it.copy(notifications = enabled) }
    override fun setPrivateSession(enabled: Boolean) = _appSettings.update { it.copy(privateSession = enabled) }

    private val _queue = MutableStateFlow(sampleTracks)
    override val queue: StateFlow<List<Track>> = _queue.asStateFlow()

    private val _nowPlaying = MutableStateFlow(
        NowPlaying(
            track = glassCeiling,
            sourceLabel = "Recently Played",
            positionMs = 108_000,
            durationMs = glassCeiling.durationMs,
            isPlaying = false,
        ),
    )
    override val nowPlaying: StateFlow<NowPlaying> = _nowPlaying.asStateFlow()

    override fun playQueue(tracks: List<Track>, startIndex: Int, sourceLabel: String) {
        if (tracks.isEmpty()) return
        _queue.value = tracks
        val track = tracks[startIndex.coerceIn(tracks.indices)]
        _nowPlaying.update {
            it.copy(
                track = track,
                sourceLabel = sourceLabel,
                positionMs = 0L,
                durationMs = track.durationMs,
                isPlaying = true,
            )
        }
    }

    override fun togglePlayPause() = _nowPlaying.update { it.copy(isPlaying = !it.isPlaying) }

    override fun skipNext() = stepQueue(1)

    override fun skipPrevious() = stepQueue(-1)

    private fun stepQueue(delta: Int) {
        val tracks = _queue.value
        if (tracks.isEmpty()) return
        val current = tracks.indexOfFirst { it.id == _nowPlaying.value.track.id }
        val next = tracks[((if (current < 0) 0 else current) + delta).mod(tracks.size)]
        _nowPlaying.update {
            it.copy(track = next, positionMs = 0L, durationMs = next.durationMs)
        }
    }

    override fun seekTo(fraction: Float) = _nowPlaying.update {
        it.copy(positionMs = (it.durationMs * fraction.coerceIn(0f, 1f)).toLong())
    }

    override fun toggleLike(trackId: String) = _nowPlaying.update {
        if (it.track.id == trackId) it.copy(track = it.track.copy(isLiked = !it.track.isLiked)) else it
    }

    override fun startSync(friendName: String) =
        _nowPlaying.update { it.copy(listeningStatus = ListeningStatus.Synced(friendName)) }

    override fun endSync() = _nowPlaying.update { it.copy(listeningStatus = ListeningStatus.Solo) }
}
