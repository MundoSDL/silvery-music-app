package com.silverymusic.app.ui.screens.liked

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.silverymusic.app.data.AppContainer
import com.silverymusic.app.theme.SilveryBackground
import com.silverymusic.app.theme.SilveryTheme
import com.silverymusic.app.ui.components.SilveryTopBar
import com.silverymusic.app.ui.components.TrackRow
import com.silverymusic.app.ui.silveryViewModel

@Composable
fun LikedSongsScreen(
    onBack: () -> Unit,
    viewModel: LikedSongsViewModel = silveryViewModel { LikedSongsViewModel(AppContainer.musicRepository) },
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        SilveryTopBar(title = "Liked Songs", onBack = onBack)

        if (uiState.isEmpty) {
            EmptyLiked()
        } else {
            LikedHeader(count = uiState.tracks.size, onPlayAll = viewModel::onPlayAll)
            LazyColumn(contentPadding = PaddingValues(horizontal = 24.dp, vertical = 4.dp)) {
                items(uiState.tracks, key = { it.id }) { track ->
                    TrackRow(
                        track = track,
                        onClick = { viewModel.onPlayTrack(track) },
                        onLikeClick = { viewModel.onToggleLike(track) },
                    )
                }
            }
        }
    }
}

@Composable
private fun LikedHeader(count: Int, onPlayAll: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "$count ${if (count == 1) "song" else "songs"}",
            style = MaterialTheme.typography.bodyMedium,
            color = SilveryTheme.colors.textTertiary,
            modifier = Modifier.weight(1f),
        )
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onBackground)
                .clickable(onClick = onPlayAll),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "Play all",
                tint = SilveryBackground,
                modifier = Modifier.size(26.dp),
            )
        }
    }
}

@Composable
private fun EmptyLiked() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(SilveryTheme.colors.liked, SilveryTheme.colors.artPlaceholder),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(32.dp),
            )
        }
        Text(
            text = "No liked songs yet",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.padding(top = 20.dp),
        )
        Text(
            text = "Tap the heart on any track and it lands here — your own collection, no algorithm involved.",
            style = MaterialTheme.typography.bodyMedium,
            color = SilveryTheme.colors.textTertiary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}
