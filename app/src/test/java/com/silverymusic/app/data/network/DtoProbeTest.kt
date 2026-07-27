package com.silverymusic.app.data.network

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pins the DTOs against real captured Jamendo responses — the payloads below are
 * verbatim from the live API. The endpoint shapes were originally written from
 * documentation alone, so this is the cheapest guard against a silent decode
 * regression. Note `/radios/` returns a bare numeric `id` where every other
 * endpoint quotes it; that only decodes because `Json.isLenient` is on.
 */
class DtoProbeTest {

    private val json: Json = NetworkFactory.json

    @Test fun tracks() {
        val e = json.decodeFromString<JamendoEnvelope<JamendoTrackDto>>(TRACKS)
        assertTrue(e.headers.isSuccess); assertEquals(1, e.results.size)
        println("TRACK ok id=" + e.results[0].id + " dur=" + e.results[0].duration + " audio=" + (e.results[0].audio != null))
    }

    @Test fun radios_intId() {
        val e = json.decodeFromString<JamendoEnvelope<JamendoRadioDto>>(RADIOS)
        println("RADIO ok id=" + e.results[0].id + " name=" + e.results[0].name)
    }

    @Test fun albums() {
        val e = json.decodeFromString<JamendoEnvelope<JamendoAlbumDto>>(ALBUMS)
        println("ALBUM ok id=" + e.results[0].id)
    }

    @Test fun artists() {
        val e = json.decodeFromString<JamendoEnvelope<JamendoArtistDto>>(ARTISTS)
        println("ARTIST ok id=" + e.results[0].id)
    }

    @Test fun playlists() {
        val e = json.decodeFromString<JamendoEnvelope<JamendoPlaylistDto>>(PLAYLISTS)
        println("PLAYLIST ok id=" + e.results[0].id)
    }

    private companion object {
        const val TRACKS = """{"headers": {"status": "success", "code": 0, "error_message": "", "warnings": "", "results_count": 2, "next": "https://api.jamendo.com/v3.0/tracks?client_id=e2ca13b0&format=json&limit=2&offset=2"}, "results": [{"id": "168", "name": "J'm'e FPM", "duration": 183, "artist_id": "7", "artist_name": "TriFace", "artist_idstr": "triface", "album_name": "Premiers Jets", "album_id": "24", "license_ccurl": "", "position": 1, "releasedate": "2004-12-17", "album_image": "https://usercontent.jamendo.com?type=album&id=24&width=300&trackid=168", "audio": "https://prod-1.storage.jamendo.com/?trackid=168&format=mp31&from=8ZuqGZ7ghAwTBp9ecGUIfw%3D%3D%7CJWRLYn9GwxIcIalwCv49pQ%3D%3D", "audiodownload": "https://prod-1.storage.jamendo.com/download/track/168/mp32/", "prourl": "", "shorturl": "https://jamen.do/t/168", "shareurl": "https://www.jamendo.com/track/168", "audiodownload_allowed": true, "content_id_free": false, "image": "https://usercontent.jamendo.com?type=album&id=24&width=300&trackid=168"}]}"""
        const val RADIOS = """{"headers": {"status": "success", "code": 0, "error_message": "", "warnings": "", "results_count": 2, "next": "https://api.jamendo.com/v3.0/radios?client_id=e2ca13b0&format=json&limit=2&offset=2"}, "results": [{"id": 1, "name": "bestof", "dispname": "Best Of Jamendo Radio", "type": "www", "image": "https://images.jamendo.com/new_jamendo_radios/bestof150.jpg"}]}"""
        const val ALBUMS = """{"headers": {"status": "success", "code": 0, "error_message": "", "warnings": "", "results_count": 2, "next": "https://api.jamendo.com/v3.0/albums?client_id=e2ca13b0&format=json&limit=2&offset=2"}, "results": [{"id": "24", "name": "Premiers Jets", "releasedate": "2004-12-17", "artist_id": "7", "artist_name": "TriFace", "image": "https://usercontent.jamendo.com?type=album&id=24&width=300", "zip": "https://storage.jamendo.com/download/a24/mp32/", "shorturl": "https://jamen.do/l/a24", "shareurl": "https://www.jamendo.com/list/a24", "zip_allowed": true}]}"""
        const val ARTISTS = """{"headers": {"status": "success", "code": 0, "error_message": "", "warnings": "", "results_count": 2, "next": "https://api.jamendo.com/v3.0/artists?client_id=e2ca13b0&format=json&limit=2&offset=2"}, "results": [{"id": "5", "name": "Both", "website": "http://www.both-world.com", "joindate": "2004-07-04", "image": "https://usercontent.jamendo.com?type=artist&id=5&width=300", "shorturl": "https://jamen.do/a/5", "shareurl": "https://www.jamendo.com/artist/5"}]}"""
        const val PLAYLISTS = """{"headers": {"status": "success", "code": 0, "error_message": "", "warnings": "", "results_count": 2, "next": "https://api.jamendo.com/v3.0/playlists?client_id=e2ca13b0&format=json&limit=2&offset=2"}, "results": [{"id": "1000", "name": "Franz\u00f6sisches", "creationdate": "2006-10-17", "user_id": "42609", "user_name": "Alberta", "zip": "https://storage.jamendo.com/download/p1000/mp32/", "shorturl": "https://jamen.do/l/p1000", "shareurl": "https://www.jamendo.com/list/p1000"}]}"""
    }
}
