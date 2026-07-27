package com.silverymusic.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.silverymusic.app.theme.SilveryTheme

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    trailingText: String? = null,
    onTrailingClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = title, style = MaterialTheme.typography.titleLarge)
        if (trailingText != null) {
            val trailingModifier = if (onTrailingClick != null) {
                Modifier.clickable(onClick = onTrailingClick)
            } else {
                Modifier
            }
            Text(
                text = trailingText,
                style = MaterialTheme.typography.bodyMedium,
                color = SilveryTheme.colors.textTertiary,
                modifier = trailingModifier,
            )
        }
    }
}
