package com.silverymusic.app.data.network

import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * LRCLIB is a community lyrics database with no auth, only a required
 * User-Agent. It was returning 5xx while this was written, so every caller
 * treats a failure here as "no lyrics from this tier" and moves on.
 */
internal interface LrcLibService {

    @GET("api/get")
    suspend fun get(
        @Query("artist_name") artistName: String,
        @Query("track_name") trackName: String,
        @Query("album_name") albumName: String? = null,
        /** Seconds. LRCLIB uses it to disambiguate releases. */
        @Query("duration") durationSeconds: Int? = null,
    ): LrcLibLyricsDto

    @GET("api/search")
    suspend fun search(@Query("q") query: String): List<LrcLibLyricsDto>

    companion object {
        const val BASE_URL = "https://lrclib.net/"
    }
}

@Serializable
internal data class LrcLibLyricsDto(
    val id: Long? = null,
    val trackName: String? = null,
    val artistName: String? = null,
    val albumName: String? = null,
    val duration: Double? = null,
    val instrumental: Boolean = false,
    val plainLyrics: String? = null,
    val syncedLyrics: String? = null,
)
