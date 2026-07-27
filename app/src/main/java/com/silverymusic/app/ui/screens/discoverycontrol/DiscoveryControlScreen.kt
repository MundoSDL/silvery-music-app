package com.silverymusic.app.ui.screens.discoverycontrol

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.silverymusic.app.data.AppContainer
import com.silverymusic.app.data.model.DiscoveryMode
import com.silverymusic.app.theme.CardShape
import com.silverymusic.app.theme.SilveryTheme
import com.silverymusic.app.ui.silveryViewModel

@Composable
fun DiscoveryControlScreen(
    viewModel: DiscoveryControlViewModel = silveryViewModel { DiscoveryControlViewModel(AppContainer.musicRepository) },
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
        Text(text = "Discovery Control", style = MaterialTheme.typography.headlineLarge)
        Text(
            text = "Choose how the algorithm learns your taste.",
            style = MaterialTheme.typography.bodyLarge,
            color = SilveryTheme.colors.textTertiary,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp),
        )

        DiscoveryMode.entries.forEach { mode ->
            DiscoveryModeCard(
                mode = mode,
                selected = mode == uiState.selectedMode,
                onClick = {
                    viewModel.onModeSelected(mode)
                    Toast.makeText(context, "Discovery set to ${mode.label}", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.padding(bottom = 12.dp),
            )
        }

        // The sub-genre screen isn't designed yet. Rather than a link that
        // silently does nothing, it says so — an affordance that lies is worse
        // than one that admits its limits (Ethical Architecture).
        Text(
            text = "Prefer full manual control? Fine-tune sub-genres →",
            style = MaterialTheme.typography.bodyMedium,
            color = SilveryTheme.colors.textTertiary,
            modifier = Modifier
                .padding(top = 4.dp, bottom = 8.dp)
                .clickable {
                    Toast.makeText(context, "Sub-genre tuning isn't in this build yet.", Toast.LENGTH_SHORT).show()
                },
        )

        if (uiState.isSynced) {
            Text(
                text = "Discovery Control affects only your solo listening. Synced sessions blend both listeners' settings.",
                style = MaterialTheme.typography.bodySmall,
                color = SilveryTheme.colors.textTertiary,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
            )
        }
    }
}

@Composable
private fun DiscoveryModeCard(
    mode: DiscoveryMode,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.onBackground else SilveryTheme.colors.border,
                shape = CardShape,
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = mode.label, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
            if (selected) {
                Icon(imageVector = Icons.Filled.CheckCircle, contentDescription = "Selected", tint = MaterialTheme.colorScheme.onBackground)
            }
        }
        Text(
            text = mode.description,
            style = MaterialTheme.typography.bodyMedium,
            color = SilveryTheme.colors.textTertiary,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}
