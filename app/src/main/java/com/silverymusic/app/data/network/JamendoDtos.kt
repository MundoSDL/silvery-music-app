package com.silverymusic.app.data.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Every Jamendo response is `{"headers": {...}, "results": [...]}`. Only `id`
 * and `name` are treated as guaranteed — the client_id could not be exercised
 * against the live API while this was written, so anything else degrades to a
 * default instead of failing the whole call.
 */
@Serializable
internal data class JamendoEnvelope<T>(
    val headers: JamendoHeaders = JamendoHeaders(),
    val results: List<T> = emptyList(),
)

@Serializable
internal data class JamendoHeaders(
    val status: String? = null,
    val code: Int? = null,
    @SerialName("error_message") val errorMessage: String? = null,
    @SerialName("results_count") val resultsCount: Int? = null,
) {
    val isSuccess: Boolean get() = status == null || status.equals("success", ignoreCase = true)

    companion object {
        /** Jamendo's "your API credentials are suspended / unknown" code. */
        const val CODE_BAD_CREDENTIALS = 11
    }
}

@Serializable
internal data class JamendoTrackDto(
    val id: String,
    val name: String,
    /** SECONDS, not millis. */
    val duration: Int? = null,
    @SerialName("artist_id") val artistId: String? = null,
    @SerialName("artist_name") val artistName: String? = null,
    @SerialName("album_id") val albumId: String? = null,
    @SerialName("album_name") val albumName: String? = null,
    @SerialName("album_image") val albumImage: String? = null,
    /** Streamable MP3. The CDN serves this without the API key. */
    val audio: String? = null,
    @SerialName("audiodownload") val audioDownload: String? = null,
    val image: String? = null,
    @SerialName("releasedate") val releaseDate: String? = null,
    @SerialName("shareurl") val shareUrl: String? = null,
    /** Only present with `include=lyrics`, and only for Jamendo's own catalog. */
    val lyrics: String? = null,
    val musicinfo: JamendoMusicInfoDto? = null,
)

@Serializable
internal data class JamendoMusicInfoDto(
    val vocalinstrumental: String? = null,
    val lang: String? = null,
    val tags: JamendoTagsDto? = null,
)

@Serializable
internal data class JamendoTagsDto(
    val genres: List<String> = emptyList(),
    val instruments: List<String> = emptyList(),
    val vartags: List<String> = emptyList(),
)

@Serializable
internal data class JamendoAlbumDto(
    val id: String,
    val name: String,
    @SerialName("artist_id") val artistId: String? = null,
    @SerialName("artist_name") val artistName: String? = null,
    val image: String? = null,
    @SerialName("releasedate") val releaseDate: String? = null,
    @SerialName("shareurl") val shareUrl: String? = null,
)

@Serializable
internal data class JamendoArtistDto(
    val id: String,
    val name: String,
    val image: String? = null,
    val website: String? = null,
    @SerialName("joindate") val joinDate: String? = null,
    @SerialName("shareurl") val shareUrl: String? = null,
)

@Serializable
internal data class JamendoPlaylistDto(
    val id: String,
    val name: String,
    @SerialName("creationdate") val creationDate: String? = null,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("user_name") val userName: String? = null,
    @SerialName("shareurl") val shareUrl: String? = null,
    /** Present only with `include=tracks`; we ask for counts, not payloads. */
    val tracks: List<JamendoTrackDto> = emptyList(),
)

/**
 * `/albums/tracks/` nests a leaner track shape than `/tracks/` — it omits
 * `artist_name` and `album_name`, so mapping folds in the parent album's.
 */
@Serializable
internal data class JamendoAlbumTracksDto(
    val id: String,
    val name: String,
    @SerialName("artist_id") val artistId: String? = null,
    @SerialName("artist_name") val artistName: String? = null,
    val image: String? = null,
    val tracks: List<JamendoTrackDto> = emptyList(),
)

@Serializable
internal data class JamendoRadioDto(
    // Every other endpoint returns a quoted id; /radios/ returns a bare number.
    // Kept as String for consistency — Json.isLenient accepts the numeric form.
    val id: String,
    val name: String,
    @SerialName("dispname") val displayName: String? = null,
    val image: String? = null,
    val type: String? = null,
    val stream: String? = null,
)
