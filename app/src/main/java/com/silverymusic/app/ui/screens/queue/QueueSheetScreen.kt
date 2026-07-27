package com.silverymusic.app.ui.screens.queue

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.silverymusic.app.data.AppContainer
import com.silverymusic.app.theme.SilveryTheme
import com.silverymusic.app.ui.components.TrackRow
import com.silverymusic.app.ui.silveryViewModel

@Composable
fun QueueSheetScreen(
    viewModel: QueueViewModel = silveryViewModel { QueueViewModel(AppContainer.musicRepository) },
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
        Text(text = "Up Next", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))

        uiState.nowPlaying?.let { nowPlaying ->
            Text(
                text = "NOW PLAYING",
                style = MaterialTheme.typography.labelMedium,
                color = SilveryTheme.colors.textTertiary,
            )
            TrackRow(track = nowPlaying.track)
        }

        Text(
            text = "NEXT",
            style = MaterialTheme.typography.labelMedium,
            color = SilveryTheme.colors.textTertiary,
            modifier = Modifier.padding(top = 12.dp),
        )
        if (uiState.isEmpty) {
            Text(
                text = "Nothing queued after this track.",
                style = MaterialTheme.typography.bodyMedium,
                color = SilveryTheme.colors.textTertiary,
                modifier = Modifier.padding(vertical = 12.dp),
            )
        }
        uiState.upNext.forEach { track ->
            TrackRow(track = track, onClick = { viewModel.onPlayTrack(track) })
        }
    }
}
