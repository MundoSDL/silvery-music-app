package com.silverymusic.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.silverymusic.app.data.model.Track
import com.silverymusic.app.theme.SilveryTheme

@Composable
fun TrackRow(
    track: Track,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onLikeClick: () -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Artwork(
            url = track.artworkUrl,
            contentDescription = null,
            modifier = Modifier.size(width = 41.dp, height = 38.dp),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
        ) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 1,
            )
            Text(
                text = "${track.artist} · ${track.genre}",
                style = MaterialTheme.typography.bodySmall,
                color = SilveryTheme.colors.textTertiary,
                maxLines = 1,
            )
        }
        Text(
            text = track.durationLabel,
            style = MaterialTheme.typography.bodyMedium,
            color = SilveryTheme.colors.textTertiary,
            modifier = Modifier.width(40.dp),
        )
        IconButton(onClick = onLikeClick) {
            Icon(
                imageVector = if (track.isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = if (track.isLiked) "Unlike" else "Like",
                tint = if (track.isLiked) SilveryTheme.colors.liked else SilveryTheme.colors.textTertiary,
            )
        }
    }
}
