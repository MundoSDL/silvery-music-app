package com.silverymusic.app.data.repository

import android.content.res.AssetManager
import com.silverymusic.app.data.DataError
import com.silverymusic.app.data.DataResult
import com.silverymusic.app.data.LyricsRepository
import com.silverymusic.app.data.model.LyricLine
import com.silverymusic.app.data.model.Lyrics
import com.silverymusic.app.data.model.LyricsSource
import com.silverymusic.app.data.model.Track
import com.silverymusic.app.data.network.JamendoService
import com.silverymusic.app.data.network.LrcLibService
import com.silverymusic.app.data.network.parseLrc
import com.silverymusic.app.data.network.parsePlainLyrics
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * Three tiers, cheapest and most reliable first:
 *
 * 1. Jamendo `include=lyrics` — same request domain as the catalog, plain text.
 * 2. LRCLIB — timestamped LRC when it has a match. It was serving 5xx while this
 *    was written, so it is strictly best-effort and never blocks the result.
 * 3. Bundled `.lrc` in assets — guarantees the feature demos with no network.
 *
 * [jamendo] and [lrcLib] are nullable so the bundled tier still works when the
 * app is running without a Jamendo client_id.
 */
internal class DefaultLyricsRepository(
    private val assets: AssetManager,
    private val jamendo: JamendoService?,
    private val lrcLib: LrcLibService?,
    private val dispatcher: CoroutineDispatcher,
) : LyricsRepository {

    override suspend fun lyricsFor(track: Track): DataResult<Lyrics> = withContext(dispatcher) {
        val lyrics = fromJamendo(track) ?: fromLrcLib(track) ?: fromAssets(track)
        if (lyrics == null || lyrics.isEmpty) {
            DataResult.Failure(DataError.Unknown("No lyrics available for \"${track.title}\""))
        } else {
            DataResult.Success(lyrics)
        }
    }

    private suspend fun fromJamendo(track: Track): Lyrics? {
        val service = jamendo ?: return null
        // Jamendo ids are numeric; the offline sample ids ("t1") would 400.
        if (track.id.toLongOrNull() == null) return null
        val body = softCall {
            service.trackById(track.id).results.firstOrNull()?.lyrics
        } ?: return null
        val lines = parsePlainLyrics(body)
        return lines.takeIf { it.isNotEmpty() }?.let {
            Lyrics(trackId = track.id, lines = it, isSynced = false, source = LyricsSource.JAMENDO)
        }
    }

    private suspend fun fromLrcLib(track: Track): Lyrics? {
        val service = lrcLib ?: return null
        val durationSeconds = (track.durationMs / 1000).toInt().takeIf { it > 0 }
        val direct = softCall {
            service.get(
                artistName = track.artist,
                trackName = track.title,
                albumName = track.albumName,
                durationSeconds = durationSeconds,
            )
        }
        val match = direct ?: softCall {
            service.search("${track.title} ${track.artist}").firstOrNull()
        } ?: return null

        match.syncedLyrics?.takeIf { it.isNotBlank() }?.let { synced ->
            val lines = parseLrc(synced)
            if (lines.isNotEmpty()) {
                return Lyrics(track.id, lines, isSynced = true, source = LyricsSource.LRCLIB)
            }
        }
        match.plainLyrics?.takeIf { it.isNotBlank() }?.let { plain ->
            val lines = parsePlainLyrics(plain)
            if (lines.isNotEmpty()) {
                return Lyrics(track.id, lines, isSynced = false, source = LyricsSource.LRCLIB)
            }
        }
        return null
    }

    private fun fromAssets(track: Track): Lyrics? {
        val body = assetCandidates(track).firstNotNullOfOrNull { readAsset(it) } ?: return null
        val synced = parseLrc(body)
        val lines: List<LyricLine>
        val isSynced: Boolean
        if (synced.isNotEmpty()) {
            lines = synced
            isSynced = true
        } else {
            lines = parsePlainLyrics(body)
            isSynced = false
        }
        return lines.takeIf { it.isNotEmpty() }
            ?.let { Lyrics(track.id, it, isSynced = isSynced, source = LyricsSource.BUNDLED) }
    }

    private fun assetCandidates(track: Track): List<String> = listOf(
        "$ASSET_DIR/${track.id.slug()}.lrc",
        "$ASSET_DIR/${track.title.slug()}.lrc",
        "$ASSET_DIR/${track.title.slug()}-${track.artist.slug()}.lrc",
    )

    private fun readAsset(path: String): String? = try {
        assets.open(path).bufferedReader().use { it.readText() }
    } catch (_: java.io.IOException) {
        null // Just means this demo track has no bundled file.
    }

    /** Runs a best-effort remote call: any failure means "this tier has nothing". */
    private suspend fun <T> softCall(block: suspend () -> T): T? = try {
        block()
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (_: Throwable) {
        null
    }

    private companion object {
        const val ASSET_DIR = "lyrics"
        val NON_SLUG = Regex("[^a-z0-9]+")

        fun String.slug(): String =
            lowercase().replace(NON_SLUG, "-").trim('-')
    }
}
