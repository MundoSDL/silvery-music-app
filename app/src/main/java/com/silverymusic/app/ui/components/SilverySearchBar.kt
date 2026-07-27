package com.silverymusic.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.silverymusic.app.theme.SearchBarShape
import com.silverymusic.app.theme.SilveryTheme

/**
 * The pill search field that tops Home/Discover/Library/Search. On the tab
 * screens it's a navigation entry point ([onClick], read-only); on the Search
 * screen itself it's editable ([onValueChange] drives real local filtering).
 */
@Composable
fun SilverySearchBar(
    modifier: Modifier = Modifier,
    value: String = "",
    onValueChange: (String) -> Unit = {},
    readOnly: Boolean = true,
    onClick: (() -> Unit)? = null,
    placeholder: String = "Search songs, albums, artists...",
) {
    val shapeModifier = modifier
        .fillMaxWidth()
        .clip(SearchBarShape)
        .background(MaterialTheme.colorScheme.surface)
        .let { if (readOnly && onClick != null) it.clickable(onClick = onClick) else it }
        .padding(horizontal = 16.dp, vertical = 12.dp)

    Row(modifier = shapeModifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = null,
            tint = SilveryTheme.colors.iconInactive,
            modifier = Modifier.padding(end = 12.dp),
        )
        if (readOnly) {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyLarge,
                color = SilveryTheme.colors.textMuted,
            )
        } else {
            val interactionSource = remember { MutableInteractionSource() }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.onSurface),
                interactionSource = interactionSource,
                decorationBox = { inner ->
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyLarge,
                            color = SilveryTheme.colors.textMuted,
                        )
                    }
                    inner()
                },
            )
        }
    }
}
