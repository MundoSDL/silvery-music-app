package com.silverymusic.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.silverymusic.app.data.model.NowPlaying
import com.silverymusic.app.theme.SilveryTheme

@Composable
fun MiniPlayerBar(
    nowPlaying: NowPlaying,
    modifier: Modifier = Modifier,
    onOpenPlayer: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onToggleLike: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SilveryTheme.colors.miniPlayerSurface)
            .clickable(onClick = onOpenPlayer),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(69.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Artwork(
                url = nowPlaying.track.artworkUrl,
                contentDescription = null,
                modifier = Modifier.size(45.dp),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
            ) {
                Text(
                    text = nowPlaying.track.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                )
                Text(
                    text = nowPlaying.track.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = SilveryTheme.colors.textTertiary,
                    maxLines = 1,
                )
            }
            IconButton(onClick = onToggleLike) {
                Icon(
                    imageVector = if (nowPlaying.track.isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = if (nowPlaying.track.isLiked) "Unlike" else "Like",
                    tint = if (nowPlaying.track.isLiked) SilveryTheme.colors.liked else MaterialTheme.colorScheme.onSurface,
                )
            }
            IconButton(onClick = onTogglePlayPause) {
                Icon(
                    imageVector = if (nowPlaying.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (nowPlaying.isPlaying) "Pause" else "Play",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
            IconButton(onClick = onSkipNext) {
                Icon(
                    imageVector = Icons.Filled.SkipNext,
                    contentDescription = "Next",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(SilveryTheme.colors.surfaceAlt),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(nowPlaying.positionFraction.coerceIn(0f, 1f))
                    .height(2.dp)
                    .background(MaterialTheme.colorScheme.onSurface),
            )
        }
    }
}
