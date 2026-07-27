package com.silverymusic.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.silverymusic.app.data.model.Artist
import com.silverymusic.app.theme.CardShape
import com.silverymusic.app.theme.SilveryTheme

/**
 * The grey placeholder is painted underneath rather than passed to Coil, so a
 * null URL, a load in flight and a failed load all settle on the same fill the
 * design shipped with — artwork appears when it arrives, nothing flickers.
 */
@Composable
fun Artwork(
    url: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    shape: Shape = CardShape,
    placeholder: Brush = SolidColor(SilveryTheme.colors.artPlaceholder),
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(placeholder),
    ) {
        if (!url.isNullOrBlank()) {
            AsyncImage(
                model = url,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
            )
        }
    }
}

@Composable
fun ArtistCircle(
    artist: Artist,
    modifier: Modifier = Modifier,
    size: Dp = 58.dp,
    onClick: (() -> Unit)? = null,
) {
    Artwork(
        url = artist.imageUrl,
        contentDescription = artist.name,
        shape = CircleShape,
        modifier = modifier
            .size(size)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
    )
}
