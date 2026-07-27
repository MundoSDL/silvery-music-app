package com.silverymusic.app.data.network

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * `client_id` and `format=json` are added by [JamendoCredentialsInterceptor],
 * so no endpoint has to repeat them.
 */
internal interface JamendoService {

    @GET("tracks/")
    suspend fun tracks(
        @Query("limit") limit: Int = DEFAULT_LIMIT,
        @Query("offset") offset: Int = 0,
        @Query("order") order: String? = null,
        @Query("namesearch") nameSearch: String? = null,
        @Query("artist_id") artistId: String? = null,
        @Query("search") search: String? = null,
        @Query("tags") tags: String? = null,
        @Query("fuzzytags") fuzzyTags: String? = null,
        @Query("include") include: String? = INCLUDE_MUSICINFO,
        @Query("imagesize") imageSize: Int = DEFAULT_IMAGE_SIZE,
        @Query("audioformat") audioFormat: String = "mp32",
        @Query("groupby") groupBy: String? = null,
    ): JamendoEnvelope<JamendoTrackDto>

    /** Single-track lookup used by the lyrics chain (`include=lyrics+musicinfo`). */
    @GET("tracks/")
    suspend fun trackById(
        @Query("id") id: String,
        @Query("include") include: String = INCLUDE_LYRICS,
    ): JamendoEnvelope<JamendoTrackDto>

    @GET("albums/")
    suspend fun albums(
        @Query("limit") limit: Int = DEFAULT_LIMIT,
        @Query("offset") offset: Int = 0,
        @Query("imagesize") imageSize: Int = DEFAULT_IMAGE_SIZE,
    ): JamendoEnvelope<JamendoAlbumDto>

    @GET("artists/")
    suspend fun artists(
        @Query("limit") limit: Int = DEFAULT_LIMIT,
        @Query("offset") offset: Int = 0,
        @Query("imagesize") imageSize: Int = DEFAULT_IMAGE_SIZE,
    ): JamendoEnvelope<JamendoArtistDto>

    @GET("playlists/")
    suspend fun playlists(
        @Query("limit") limit: Int = DEFAULT_LIMIT,
        @Query("offset") offset: Int = 0,
    ): JamendoEnvelope<JamendoPlaylistDto>

    @GET("radios/")
    suspend fun radios(
        @Query("limit") limit: Int = DEFAULT_LIMIT,
        @Query("imagesize") imageSize: Int = DEFAULT_IMAGE_SIZE,
    ): JamendoEnvelope<JamendoRadioDto>

    /**
     * Returns one album object carrying a nested `tracks` array. The nested
     * entries are leaner than a top-level track — no `artist_name`, no
     * `album_name` — so the parent's fields have to be folded in when mapping.
     */
    @GET("albums/tracks/")
    suspend fun albumTracks(
        @Query("id") id: String,
        @Query("imagesize") imageSize: Int = DEFAULT_IMAGE_SIZE,
        @Query("audioformat") audioFormat: String = "mp32",
    ): JamendoEnvelope<JamendoAlbumTracksDto>

    /** Same nesting as [albumTracks]; these entries do carry `artist_name`/`image`. */
    @GET("playlists/tracks/")
    suspend fun playlistTracks(
        @Query("id") id: String,
        @Query("audioformat") audioFormat: String = "mp32",
    ): JamendoEnvelope<JamendoPlaylistDto>

    companion object {
        const val BASE_URL = "https://api.jamendo.com/v3.0/"
        const val DEFAULT_LIMIT = 30
        const val DEFAULT_IMAGE_SIZE = 300
        const val INCLUDE_MUSICINFO = "musicinfo"
        const val INCLUDE_LYRICS = "lyrics+musicinfo"

        const val ORDER_POPULAR = "popularity_total"
        const val ORDER_NEWEST = "releasedate_desc"
    }
}
