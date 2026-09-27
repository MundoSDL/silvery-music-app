package com.silverymusic.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.silverymusic.app.data.model.NowPlaying
import com.silverymusic.app.theme.SilveryTheme
import com.silverymusic.app.ui.motion.SharedArtworkKey
import com.silverymusic.app.ui.motion.rememberSmoothPositionMs
import com.silverymusic.app.ui.motion.sharedPlayerElement

@Composable
fun MiniPlayerBar(
    nowPlaying: NowPlaying,
    modifier: Modifier = Modifier,
    onOpenPlayer: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onToggleLike: () -> Unit,
) {
    // Before anything has loaded there is no track to like, play or skip, so
    // the controls rest disabled instead of acting on the placeholder.
    val hasTrack = nowPlaying.hasTrack
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SilveryTheme.colors.miniPlayerSurface)
            .clickable(enabled = hasTrack, onClick = onOpenPlayer),
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
                // The same cover grows into the full player when the bar is tapped.
                modifier = Modifier
                    .sharedPlayerElement(SharedArtworkKey)
                    .size(45.dp),
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
                val error = nowPlaying.playbackError
                Text(
                    text = error ?: nowPlaying.track.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (error != null) SilveryTheme.colors.liked else SilveryTheme.colors.textTertiary,
                    maxLines = 1,
                )
            }
            LikeButton(
                liked = nowPlaying.track.isLiked,
                onToggle = onToggleLike,
                enabled = hasTrack,
            )
            IconButton(onClick = onTogglePlayPause, enabled = hasTrack) {
                PlayPauseIcon(isPlaying = nowPlaying.isPlaying)
            }
            IconButton(onClick = onSkipNext, enabled = hasTrack) {
                Icon(
                    imageVector = Icons.Filled.SkipNext,
                    contentDescription = "Next",
                )
            }
        }
        ProgressLine(nowPlaying)
    }
}

/**
 * Two-pixel progress line. Drawn from an extrapolated position so it glides
 * every frame, and read only in the draw phase so the bar never recomposes
 * just to move it.
 */
@Composable
private fun ProgressLine(nowPlaying: NowPlaying) {
    val position by rememberSmoothPositionMs(nowPlaying.positionMs, nowPlaying.isPlaying, nowPlaying.durationMs)
    val duration = nowPlaying.durationMs
    val trackColor = SilveryTheme.colors.surfaceAlt
    val fillColor = MaterialTheme.colorScheme.onSurface
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(2.dp)
            .drawBehind {
                drawRect(trackColor)
                val fraction = if (duration > 0L) (position.toFloat() / duration).coerceIn(0f, 1f) else 0f
                drawRect(fillColor, topLeft = Offset.Zero, size = Size(size.width * fraction, size.height))
            },
    )
}
