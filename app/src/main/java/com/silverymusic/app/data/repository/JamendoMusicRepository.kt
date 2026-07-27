package com.silverymusic.app.data.repository

import com.silverymusic.app.data.DataResult
import com.silverymusic.app.data.FakeMusicRepository
import com.silverymusic.app.data.MusicRepository
import com.silverymusic.app.data.getOrNull
import com.silverymusic.app.data.map
import com.silverymusic.app.data.model.AppSettings
import com.silverymusic.app.data.model.Artist
import com.silverymusic.app.data.model.AudioQuality
import com.silverymusic.app.data.model.DiscoveryMode
import com.silverymusic.app.data.model.EqPreset
import com.silverymusic.app.data.model.EqSettings
import com.silverymusic.app.data.model.Genre
import com.silverymusic.app.data.model.NowPlaying
import com.silverymusic.app.data.model.Playlist
import com.silverymusic.app.data.model.Profile
import com.silverymusic.app.data.model.RepeatMode
import com.silverymusic.app.data.model.Track
import com.silverymusic.app.data.network.JamendoService
import com.silverymusic.app.data.network.jamendoResults
import com.silverymusic.app.data.network.toArtist
import com.silverymusic.app.data.network.toPlaylist
import com.silverymusic.app.data.network.toTrack
import com.silverymusic.app.data.network.toTracks
import com.silverymusic.app.playback.PlaybackController
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.StateFlow

/**
 * The live catalog: every browse/search surface is a Jamendo query, playback is
 * delegated to [playback], and the demo-only state (profiles, EQ, settings,
 * discovery mode) reuses [FakeMusicRepository] in memory — this build has no
 * database on purpose.
 */
internal class JamendoMusicRepository(
    private val service: JamendoService,
    private val playback: PlaybackController,
    private val dispatcher: CoroutineDispatcher,
    private val local: FakeMusicRepository = FakeMusicRepository(),
) : MusicRepository {

    // ---- Catalog -----------------------------------------------------------

    // Home's two hero rows come from /albums/, not /playlists/: Jamendo playlists
    // carry no image field at all, which left every tile on the landing screen
    // showing the grey placeholder. Albums always have cover art.
    override suspend fun recentlyPlayed(): DataResult<List<Playlist>> =
        jamendoResults(dispatcher) { service.albums(limit = 8) }
            .map { dtos -> dtos.map { it.toPlaylist() } }

    override suspend fun madeForYou(): DataResult<List<Playlist>> =
        jamendoResults(dispatcher) { service.albums(limit = 6, offset = 8) }
            .map { dtos -> dtos.map { it.toPlaylist() } }

    override suspend fun topGenres(): DataResult<List<Genre>> =
        DataResult.Success(GENRES.take(4))

    override suspend fun yourArtists(): DataResult<List<Artist>> =
        jamendoResults(dispatcher) { service.artists(limit = 12) }
            .map { dtos -> dtos.map { it.toArtist() } }

    /**
     * Jamendo has no "current vibe" concept, so it's derived: the genres of the
     * most popular tracks right now, presented as one playlist card.
     */
    override suspend fun currentVibe(): DataResult<Playlist> =
        popularTracks(limit = 12).map { tracks ->
            val genres = tracks.map { it.genre }.distinct().take(3)
            Playlist(
                id = VIBE_ID,
                title = "Popular on Jamendo",
                subtitle = genres.joinToString(" · ").ifBlank { "Fresh from the catalog" },
                trackCount = tracks.size,
                artworkUrl = tracks.firstOrNull()?.artworkUrl,
            )
        }

    override suspend fun discoverQueue(): DataResult<List<Track>> = popularTracks(limit = 20)

    override suspend fun browseGenres(): DataResult<List<Genre>> = DataResult.Success(GENRES.take(5))

    override suspend fun libraryPlaylists(): DataResult<List<Playlist>> =
        jamendoResults(dispatcher) { service.playlists(limit = 20) }
            .map { dtos -> dtos.map { it.toPlaylist() } }

    override suspend fun libraryAlbums(): DataResult<List<Playlist>> =
        jamendoResults(dispatcher) { service.albums(limit = 20) }
            .map { dtos -> dtos.map { it.toPlaylist() } }

    override suspend fun libraryArtists(): DataResult<List<Artist>> =
        jamendoResults(dispatcher) { service.artists(limit = 20) }
            .map { dtos -> dtos.map { it.toArtist() } }

    override suspend fun libraryRadio(): DataResult<List<Playlist>> =
        jamendoResults(dispatcher) { service.radios(limit = 10) }
            .map { dtos -> dtos.map { it.toPlaylist() } }

    override suspend fun searchGenres(): DataResult<List<Genre>> = DataResult.Success(GENRES)

    override suspend fun trendingNow(): DataResult<List<Track>> = popularTracks(limit = 25)

    override suspend fun searchTracks(query: String): DataResult<List<Track>> {
        val trimmed = query.trim()
        // Jamendo rejects a namesearch shorter than 2 chars with a hard type
        // error (code 3), which the first keystroke would otherwise trigger.
        // Treat those like an empty query and keep showing trending.
        if (trimmed.length < MIN_SEARCH_LENGTH) return trendingNow()
        return jamendoResults(dispatcher) {
            service.tracks(limit = 25, nameSearch = trimmed)
        }.map { dtos -> dtos.map { it.toTrack() } }
    }

    /**
     * The mappers prefix ids by kind (`album-`, `playlist-`, `radio-`), which is
     * what lets one domain [Playlist] type cover three different Jamendo
     * entities and still be expanded correctly here.
     */
    override suspend fun tracksFor(playlist: Playlist): DataResult<List<Track>> {
        val rawId = playlist.id.substringAfter('-', missingDelimiterValue = playlist.id)
        val primary = when {
            // The synthesized Discover hero has no Jamendo entity behind it.
            playlist.id == VIBE_ID -> return discoverQueue()

            playlist.id.startsWith("album-") -> jamendoResults(dispatcher) {
                service.albumTracks(id = rawId)
            }.map { albums -> albums.flatMap { it.toTracks() } }

            playlist.id.startsWith("playlist-") -> jamendoResults(dispatcher) {
                service.playlistTracks(id = rawId)
            }.map { lists -> lists.flatMap { list -> list.tracks.map { it.toTrack() } } }

            // Jamendo radios are live streams with no track listing, so the
            // closest honest expansion is popular tracks tagged with its name.
            playlist.id.startsWith("radio-") -> return jamendoResults(dispatcher) {
                service.tracks(limit = 25, tags = playlist.title.lowercase(), order = JamendoService.ORDER_POPULAR)
            }.map { dtos -> dtos.map { it.toTrack() } }

            else -> return DataResult.Success(emptyList())
        }
        // A large share of Jamendo playlists/albums are genuinely empty. Rather
        // than a dead tap, backstop with popular tracks so playback always starts.
        return if (primary is DataResult.Success && primary.data.isEmpty()) {
            popularTracks(GENRE_LIMIT)
        } else {
            primary
        }
    }

    /**
     * Jamendo's tag index is inconsistent — the identical query can return a
     * full page, then nothing a second later. For a browsing surface that's
     * indistinguishable from "this genre is empty", so each strategy is tried
     * in turn and popular tracks backstop the lot. A genre tap always plays
     * something.
     */
    override suspend fun tracksForGenre(genre: Genre): DataResult<List<Track>> {
        val strategies: List<suspend () -> DataResult<List<Track>>> = listOf(
            { tagQuery(fuzzy = genre.id) },
            { tagQuery(exact = genre.id) },
            { tagQuery(fuzzy = genre.name.lowercase()) },
            { freeTextQuery(genre.name) },
        )
        for (strategy in strategies) {
            // A rung that errors or comes back empty just means "try the next";
            // popular tracks backstop the lot, so a genre tap always plays.
            val tracks = strategy().getOrNull().orEmpty()
            if (tracks.isNotEmpty()) return DataResult.Success(tracks)
        }
        return popularTracks(GENRE_LIMIT)
    }

    override suspend fun tracksForArtist(artist: Artist): DataResult<List<Track>> =
        jamendoResults(dispatcher) {
            service.tracks(limit = GENRE_LIMIT, artistId = artist.id, order = JamendoService.ORDER_POPULAR)
        }.map { dtos -> dtos.map { it.toTrack() } }

    private suspend fun tagQuery(exact: String? = null, fuzzy: String? = null): DataResult<List<Track>> =
        jamendoResults(dispatcher) {
            service.tracks(
                limit = GENRE_LIMIT,
                tags = exact,
                fuzzyTags = fuzzy,
                order = JamendoService.ORDER_POPULAR,
            )
        }.map { dtos -> dtos.map { it.toTrack() } }

    private suspend fun freeTextQuery(term: String): DataResult<List<Track>> =
        jamendoResults(dispatcher) {
            service.tracks(limit = GENRE_LIMIT, search = term, order = JamendoService.ORDER_POPULAR)
        }.map { dtos -> dtos.map { it.toTrack() } }

    private suspend fun popularTracks(limit: Int): DataResult<List<Track>> =
        jamendoResults(dispatcher) {
            service.tracks(limit = limit, order = JamendoService.ORDER_POPULAR)
        }.map { dtos ->
            dtos.map { it.toTrack() }.also { tracks -> playback.primeQueue(tracks, "Popular on Jamendo") }
        }

    // ---- Playback ----------------------------------------------------------

    override val nowPlaying: StateFlow<NowPlaying> get() = playback.nowPlaying
    override val queue: StateFlow<List<Track>> get() = playback.queue
    override val likedTracks: StateFlow<List<Track>> get() = playback.likedTracks
    override val repeatMode: StateFlow<RepeatMode> get() = playback.repeatMode

    override fun playQueue(tracks: List<Track>, startIndex: Int, sourceLabel: String) =
        playback.playQueue(tracks, startIndex, sourceLabel)

    override fun togglePlayPause() = playback.togglePlayPause()
    override fun skipNext() = playback.skipNext()
    override fun skipPrevious() = playback.skipPrevious()
    override fun seekTo(fraction: Float) = playback.seekTo(fraction)
    override fun toggleLike(trackId: String) = playback.toggleLike(trackId)
    override fun startSync(friendName: String) = playback.startSync(friendName)
    override fun endSync() = playback.endSync()
    override fun cycleRepeatMode() = playback.cycleRepeatMode()
    override fun shuffleQueue() = playback.shuffleQueue()

    // ---- In-memory demo state (delegated, not duplicated) -------------------

    override val profiles: StateFlow<List<Profile>> get() = local.profiles
    override val activeProfileId: StateFlow<String> get() = local.activeProfileId
    override fun selectProfile(profileId: String) = local.selectProfile(profileId)
    override fun addProfile(name: String, isKid: Boolean, accentIndex: Int) =
        local.addProfile(name, isKid, accentIndex)

    override fun renameProfile(profileId: String, name: String) = local.renameProfile(profileId, name)
    override fun removeProfile(profileId: String) = local.removeProfile(profileId)

    override val discoveryMode: StateFlow<DiscoveryMode> get() = local.discoveryMode
    override fun setDiscoveryMode(mode: DiscoveryMode) = local.setDiscoveryMode(mode)

    // EQ state stays in [local] for the UI to render, but every change is also
    // pushed to the player so it actually reshapes the audio, not just the curve.
    override val eqSettings: StateFlow<EqSettings> get() = local.eqSettings
    override fun setEqEnabled(enabled: Boolean) {
        local.setEqEnabled(enabled)
        playback.applyEqualizer(local.eqSettings.value)
    }

    override fun setEqPreset(preset: EqPreset) {
        local.setEqPreset(preset)
        playback.applyEqualizer(local.eqSettings.value)
    }

    override fun setEqBandGain(bandIndex: Int, gainDb: Float) {
        local.setEqBandGain(bandIndex, gainDb)
        playback.applyEqualizer(local.eqSettings.value)
    }

    override fun resetEq() {
        local.resetEq()
        playback.applyEqualizer(local.eqSettings.value)
    }

    override val appSettings: StateFlow<AppSettings> get() = local.appSettings
    override fun setAudioQuality(quality: AudioQuality) = local.setAudioQuality(quality)
    override fun setGaplessPlayback(enabled: Boolean) = local.setGaplessPlayback(enabled)
    override fun setVolumeNormalization(enabled: Boolean) = local.setVolumeNormalization(enabled)
    override fun setAutoplaySimilar(enabled: Boolean) = local.setAutoplaySimilar(enabled)
    override fun setNotifications(enabled: Boolean) = local.setNotifications(enabled)
    override fun setPrivateSession(enabled: Boolean) = local.setPrivateSession(enabled)

    private companion object {
        const val GENRE_LIMIT = 25

        /** Jamendo's namesearch rejects anything shorter than this with a type error. */
        const val MIN_SEARCH_LENGTH = 2

        /** The Discover hero is synthesized, so its id maps to no Jamendo entity. */
        const val VIBE_ID = "vibe"

        /**
         * Jamendo exposes no genre-list endpoint, so the design's genre set is
         * fixed here. Ids double as Jamendo tag slugs for future tag queries.
         */
        val GENRES = listOf(
            Genre("ambient", "Ambient"),
            Genre("indie", "Indie"),
            Genre("electronic", "Electronic"),
            Genre("lofi", "Lo-fi"),
            Genre("jazz", "Jazz"),
            Genre("hiphop", "Hip-Hop"),
            Genre("classical", "Classical"),
            Genre("pop", "Pop"),
        )
    }
}
