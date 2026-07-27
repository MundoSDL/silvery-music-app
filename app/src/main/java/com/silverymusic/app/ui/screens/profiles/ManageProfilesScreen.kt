package com.silverymusic.app.ui.screens.profiles

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.silverymusic.app.data.AppContainer
import com.silverymusic.app.data.model.Profile
import com.silverymusic.app.theme.CardShape
import com.silverymusic.app.theme.PillShape
import com.silverymusic.app.theme.SilveryTheme
import com.silverymusic.app.ui.components.ProfileAvatar
import com.silverymusic.app.ui.components.SilveryTopBar
import com.silverymusic.app.ui.silveryViewModel

@Composable
fun ManageProfilesScreen(
    onBack: () -> Unit,
    onAddProfile: () -> Unit,
    viewModel: ManageProfilesViewModel = silveryViewModel { ManageProfilesViewModel(AppContainer.musicRepository) },
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        SilveryTopBar(title = "Manage Profiles", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
        ) {
            Text(
                text = "Each profile keeps its own library, queue and algorithm. Nothing played on one profile shapes another.",
                style = MaterialTheme.typography.bodyMedium,
                color = SilveryTheme.colors.textTertiary,
                modifier = Modifier.padding(bottom = 20.dp),
            )

            uiState.profiles.forEach { profile ->
                ManageProfileCard(
                    profile = profile,
                    isActive = profile.id == uiState.activeProfileId,
                    onRemove = { viewModel.onRemoveRequested(profile) },
                    modifier = Modifier.padding(bottom = 12.dp),
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CardShape)
                    .border(1.dp, SilveryTheme.colors.border, CardShape)
                    .clickable(onClick = onAddProfile)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(SilveryTheme.colors.surfaceAlt)
                        .padding(13.dp),
                )
                Column(modifier = Modifier.padding(start = 16.dp)) {
                    Text(
                        text = "Add Profile",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = "Create a new listening space",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SilveryTheme.colors.textTertiary,
                    )
                }
            }
        }
    }

    uiState.pendingRemoval?.let { profile ->
        AlertDialog(
            onDismissRequest = viewModel::onRemovalDismissed,
            containerColor = SilveryTheme.colors.surfaceAlt,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = SilveryTheme.colors.textSecondary,
            title = { Text(text = "Remove ${profile.name}?") },
            text = {
                Text(text = "Their playlists, history and algorithm settings are deleted. This can't be undone.")
            },
            confirmButton = {
                TextButton(onClick = viewModel::onRemovalConfirmed) {
                    Text(text = "Remove", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onRemovalDismissed) {
                    Text(text = "Cancel", color = MaterialTheme.colorScheme.onSurface)
                }
            },
        )
    }
}

@Composable
private fun ManageProfileCard(
    profile: Profile,
    isActive: Boolean,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ProfileAvatar(name = profile.name, accentIndex = profile.accentIndex)

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = profile.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (isActive) {
                    Text(
                        text = "In use",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.background,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .clip(PillShape)
                            .background(MaterialTheme.colorScheme.onBackground)
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                }
            }
            Text(
                text = profile.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = SilveryTheme.colors.textTertiary,
            )
        }

        if (profile.isRemovable) {
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Filled.DeleteOutline,
                    contentDescription = "Remove ${profile.name}",
                    tint = SilveryTheme.colors.textTertiary,
                )
            }
        }
    }
}
