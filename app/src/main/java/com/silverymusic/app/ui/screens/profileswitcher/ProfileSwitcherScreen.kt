package com.silverymusic.app.ui.screens.profileswitcher

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.silverymusic.app.data.AppContainer
import com.silverymusic.app.data.model.Profile
import com.silverymusic.app.theme.SilveryTheme
import com.silverymusic.app.ui.components.ProfileAvatar
import com.silverymusic.app.ui.silveryViewModel

@Composable
fun ProfileSwitcherScreen(
    onAddProfile: () -> Unit,
    onManageProfiles: () -> Unit,
    viewModel: ProfileSwitcherViewModel = silveryViewModel { ProfileSwitcherViewModel(AppContainer.musicRepository) },
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
        Text(
            text = "Switch Profile",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        uiState.profiles.forEach { profile ->
            ProfileRow(
                profile = profile,
                isActive = profile.id == uiState.activeProfileId,
                onClick = { viewModel.onSelectProfile(profile.id) },
            )
        }

        ProfileRow(
            profile = null,
            isActive = false,
            onClick = onAddProfile,
        )

        Text(
            text = "Manage Profiles",
            style = MaterialTheme.typography.bodyMedium,
            color = SilveryTheme.colors.textTertiary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp)
                .clickable(onClick = onManageProfiles),
        )
    }
}

@Composable
private fun ProfileRow(
    profile: Profile?,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (profile != null) {
            ProfileAvatar(name = profile.name, accentIndex = profile.accentIndex)
        } else {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(SilveryTheme.colors.surfaceAlt),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
            Text(
                text = profile?.name ?: "Add Profile",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            )
            Text(
                text = profile?.subtitle ?: "Create a new listening space",
                style = MaterialTheme.typography.bodyMedium,
                color = SilveryTheme.colors.textTertiary,
            )
        }
        if (isActive) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onBackground),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Active",
                    tint = MaterialTheme.colorScheme.background,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
    }
}
