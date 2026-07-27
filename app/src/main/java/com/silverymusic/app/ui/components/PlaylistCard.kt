package com.silverymusic.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.silverymusic.app.data.model.Playlist
import com.silverymusic.app.theme.CardShape
import com.silverymusic.app.theme.SilveryTheme

/** Compact square tile — Home "Recently Played" row. */
@Composable
fun PlaylistTile(
    playlist: Playlist,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Column(modifier = modifier.width(110.dp).clickable(onClick = onClick)) {
        Artwork(
            url = playlist.artworkUrl,
            contentDescription = playlist.title,
            placeholder = SolidColor(SilveryTheme.colors.surfaceAlt),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.08f),
        )
        Text(
            text = playlist.title,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            maxLines = 1,
            modifier = Modifier.padding(top = 8.dp),
        )
        Text(
            text = playlist.subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = SilveryTheme.colors.textTertiary,
            maxLines = 1,
        )
    }
}

/** Larger card with text on the surface itself — Home "Made For You" row. */
@Composable
fun FeaturedPlaylistCard(
    playlist: Playlist,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Box(
        modifier = modifier
            .size(width = 167.dp, height = 125.dp)
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick),
    ) {
        Artwork(
            url = playlist.artworkUrl,
            contentDescription = null,
            placeholder = SolidColor(Color.Transparent),
            modifier = Modifier.fillMaxSize(),
        )
        if (!playlist.artworkUrl.isNullOrBlank()) {
            // The title sits directly on the cover, so it needs a floor of
            // darkness under it — a pale cover would swallow the white text.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.35f to Color.Transparent,
                            1f to Color.Black.copy(alpha = 0.72f),
                        ),
                    ),
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp),
        ) {
            Text(text = playlist.title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = playlist.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = SilveryTheme.colors.textTertiary,
            )
        }
    }
}
