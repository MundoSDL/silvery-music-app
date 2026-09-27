package com.silverymusic.app.ui.screens.queue

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.silverymusic.app.data.AppContainer
import com.silverymusic.app.data.model.Track
import com.silverymusic.app.theme.SilveryTheme
import com.silverymusic.app.ui.components.TrackRow
import com.silverymusic.app.ui.motion.LocalReducedMotion
import com.silverymusic.app.ui.silveryViewModel

@Composable
fun QueueSheetScreen(
    viewModel: QueueViewModel = silveryViewModel { QueueViewModel(AppContainer.musicRepository) },
) {
    val uiState by viewModel.uiState.collectAsState()
    val reduced = LocalReducedMotion.current
    // Stable keys let rows glide to their new places when the queue is shuffled
    // or advances, instead of the whole list snapping.
    val upNext = remember(uiState.upNext) { keyed(uiState.upNext) }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
    ) {
        item(key = "header") {
            Text(text = "Up Next", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))
        }

        uiState.nowPlaying?.let { nowPlaying ->
            item(key = "now-playing-label") {
                Text(
                    text = "NOW PLAYING",
                    style = MaterialTheme.typography.labelMedium,
                    color = SilveryTheme.colors.textTertiary,
                )
            }
            item(key = "now-playing") {
                TrackRow(track = nowPlaying.track)
            }
        }

        item(key = "next-label") {
            Text(
                text = "NEXT",
                style = MaterialTheme.typography.labelMedium,
                color = SilveryTheme.colors.textTertiary,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
        if (uiState.isEmpty) {
            item(key = "empty") {
                Text(
                    text = "Nothing queued after this track.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SilveryTheme.colors.textTertiary,
                    modifier = Modifier.padding(vertical = 12.dp),
                )
            }
        }
        items(upNext, key = { it.key }) { entry ->
            TrackRow(
                track = entry.track,
                onClick = { viewModel.onPlayTrack(entry.track) },
                modifier = if (reduced) Modifier else Modifier.animateItem(),
            )
        }
    }
}

private data class QueueEntry(val key: String, val track: Track)

/** A track can sit in the queue twice, so keys count repeats to stay unique. */
private fun keyed(tracks: List<Track>): List<QueueEntry> {
    val seen = HashMap<String, Int>()
    return tracks.map { track ->
        val occurrence = seen.getOrElse(track.id) { 0 }
        seen[track.id] = occurrence + 1
        QueueEntry(key = "${track.id}#$occurrence", track = track)
    }
}
