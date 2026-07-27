package com.silverymusic.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.silverymusic.app.theme.PillShape
import com.silverymusic.app.theme.SilveryTheme

@Composable
fun GenreChip(
    label: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val clickModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier
            .clip(PillShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, SilveryTheme.colors.border, PillShape)
            .then(clickModifier)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    )
}
