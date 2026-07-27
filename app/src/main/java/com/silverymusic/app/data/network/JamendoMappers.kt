package com.silverymusic.app.data.network

import com.silverymusic.app.data.model.Artist
import com.silverymusic.app.data.model.Playlist
import com.silverymusic.app.data.model.Track

private const val UNKNOWN_ARTIST = "Unknown artist"
private const val UNKNOWN_GENRE = "Music"

internal fun JamendoTrackDto.toTrack(): Track = Track(
    id = id,
    title = name,
    artist = artistName?.takeIf { it.isNotBlank() } ?: UNKNOWN_ARTIST,
    genre = primaryGenre(),
    // Jamendo reports seconds; the whole app works in millis.
    durationMs = (duration ?: 0).toLong() * 1000L,
    streamUrl = audio?.takeIf { it.isNotBlank() } ?: audioDownload?.takeIf { it.isNotBlank() },
    artworkUrl = image?.takeIf { it.isNotBlank() } ?: albumImage?.takeIf { it.isNotBlank() },
    albumName = albumName?.takeIf { it.isNotBlank() },
)

/**
 * Nested album tracks omit the artist and cover, so the parent album supplies
 * them — otherwise every row in an album would read "Unknown artist".
 */
internal fun JamendoAlbumTracksDto.toTracks(): List<Track> = tracks.map { track ->
    track.toTrack().copy(
        artist = track.artistName?.takeIf { it.isNotBlank() }
            ?: artistName?.takeIf { it.isNotBlank() }
            ?: UNKNOWN_ARTIST,
        artworkUrl = track.image?.takeIf { it.isNotBlank() }
            ?: track.albumImage?.takeIf { it.isNotBlank() }
            ?: image?.takeIf { it.isNotBlank() },
        albumName = track.albumName?.takeIf { it.isNotBlank() } ?: name,
    )
}

private fun JamendoTrackDto.primaryGenre(): String {
    val tags = musicinfo?.tags ?: return UNKNOWN_GENRE
    val tag = tags.genres.firstOrNull() ?: tags.vartags.firstOrNull() ?: return UNKNOWN_GENRE
    return tag.replaceFirstChar { it.uppercaseChar() }
}

internal fun JamendoAlbumDto.toPlaylist(): Playlist = Playlist(
    id = "album-$id",
    title = name,
    subtitle = artistName?.takeIf { it.isNotBlank() } ?: UNKNOWN_ARTIST,
    trackCount = 0,
    artworkUrl = image?.takeIf { it.isNotBlank() },
    durationLabel = releaseDate?.take(4)?.takeIf { it.length == 4 },
)

internal fun JamendoPlaylistDto.toPlaylist(): Playlist {
    val count = tracks.size
    return Playlist(
        id = "playlist-$id",
        title = name,
        subtitle = when {
            count > 0 -> "$count songs"
            !userName.isNullOrBlank() -> "by $userName"
            else -> "Jamendo playlist"
        },
        trackCount = count,
        artworkUrl = null,
        durationLabel = null,
    )
}

internal fun JamendoRadioDto.toPlaylist(): Playlist = Playlist(
    id = "radio-$id",
    title = displayName?.takeIf { it.isNotBlank() } ?: name,
    subtitle = "Endless radio mix",
    trackCount = 0,
    artworkUrl = image?.takeIf { it.isNotBlank() },
)

internal fun JamendoArtistDto.toArtist(): Artist = Artist(
    id = id,
    name = name,
    imageUrl = image?.takeIf { it.isNotBlank() },
)
