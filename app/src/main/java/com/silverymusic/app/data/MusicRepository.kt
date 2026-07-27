package com.silverymusic.app.data

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
import kotlinx.coroutines.flow.StateFlow

/**
 * The contract the front end builds against. A fake in-memory implementation
 * ([FakeMusicRepository]) backs it for this build; a real implementation
 * (network + persistence) is the hand-off point for the back-end work.
 */
interface MusicRepository {

    // Home
    suspend fun recentlyPlayed(): DataResult<List<Playlist>>
    suspend fun madeForYou(): DataResult<List<Playlist>>
    suspend fun topGenres(): DataResult<List<Genre>>
    suspend fun yourArtists(): DataResult<List<Artist>>

    // Discover
    suspend fun currentVibe(): DataResult<Playlist>
    suspend fun discoverQueue(): DataResult<List<Track>>
    suspend fun browseGenres(): DataResult<List<Genre>>

    // Library
    suspend fun libraryPlaylists(): DataResult<List<Playlist>>
    suspend fun libraryAlbums(): DataResult<List<Playlist>>
    suspend fun libraryArtists(): DataResult<List<Artist>>
    suspend fun libraryRadio(): DataResult<List<Playlist>>

    // Search
    suspend fun searchGenres(): DataResult<List<Genre>>
    suspend fun trendingNow(): DataResult<List<Track>>

    /** Remote text search across the catalog. Empty [query] returns trending. */
    suspend fun searchTracks(query: String): DataResult<List<Track>>

    /**
     * Expands a playlist, album or radio row into playable tracks. Returns an
     * empty list rather than a failure when the source genuinely holds nothing,
     * so the UI can distinguish "empty" from "broken".
     */
    suspend fun tracksFor(playlist: Playlist): DataResult<List<Track>>

    /** Playable tracks for a genre tile/chip. */
    suspend fun tracksForGenre(genre: Genre): DataResult<List<Track>>

    /** Playable tracks for an artist row. */
    suspend fun tracksForArtist(artist: Artist): DataResult<List<Track>>

    // Profiles
    val profiles: StateFlow<List<Profile>>
    val activeProfileId: StateFlow<String>
    fun selectProfile(profileId: String)
    fun addProfile(name: String, isKid: Boolean, accentIndex: Int)
    fun renameProfile(profileId: String, name: String)
    fun removeProfile(profileId: String)

    // Discovery Control
    val discoveryMode: StateFlow<DiscoveryMode>
    fun setDiscoveryMode(mode: DiscoveryMode)

    // Equalizer
    val eqSettings: StateFlow<EqSettings>
    fun setEqEnabled(enabled: Boolean)
    fun setEqPreset(preset: EqPreset)
    fun setEqBandGain(bandIndex: Int, gainDb: Float)
    fun resetEq()

    // Settings
    val appSettings: StateFlow<AppSettings>
    fun setAudioQuality(quality: AudioQuality)
    fun setGaplessPlayback(enabled: Boolean)
    fun setVolumeNormalization(enabled: Boolean)
    fun setAutoplaySimilar(enabled: Boolean)
    fun setNotifications(enabled: Boolean)
    fun setPrivateSession(enabled: Boolean)

    // Playback
    val nowPlaying: StateFlow<NowPlaying>

    /** Replaces the queue with [tracks] and starts at [startIndex]. */
    fun playQueue(tracks: List<Track>, startIndex: Int = 0, sourceLabel: String)
    val queue: StateFlow<List<Track>>

    fun togglePlayPause()
    fun skipNext()
    fun skipPrevious()
    fun seekTo(fraction: Float)
    fun toggleLike(trackId: String)
    fun startSync(friendName: String)
    fun endSync()

    /** Tracks the user has hearted this session, newest first. */
    val likedTracks: StateFlow<List<Track>>

    /** Current repeat state; the player button cycles Off → All → One. */
    val repeatMode: StateFlow<RepeatMode>
    fun cycleRepeatMode()

    /** Randomises the not-yet-played tail of the queue without interrupting playback. */
    fun shuffleQueue()
}
