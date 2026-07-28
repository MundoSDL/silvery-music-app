package com.silverymusic.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.silverymusic.app.BuildConfig
import com.silverymusic.app.data.AppContainer
import com.silverymusic.app.data.model.AudioQuality
import com.silverymusic.app.theme.SilveryTheme
import com.silverymusic.app.ui.components.SilveryTopBar
import com.silverymusic.app.ui.silveryViewModel

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenDiscoveryControl: () -> Unit,
    onOpenManageProfiles: () -> Unit,
    onOpenHowItWorks: () -> Unit,
    onSignedOut: () -> Unit,
    viewModel: SettingsViewModel = silveryViewModel {
        SettingsViewModel(AppContainer.musicRepository, AppContainer.authRepository)
    },
) {
    val uiState by viewModel.uiState.collectAsState()
    val settings = uiState.settings

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        SilveryTopBar(title = "Settings", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            SettingsSection(title = "Audio") {
                SettingsNavRow(
                    title = "Audio quality",
                    value = settings.audioQuality.label,
                    onClick = { viewModel.onQualityPickerVisibilityChange(true) },
                )
                SettingsDivider()
                SettingsNavRow(title = "Equalizer", onClick = onOpenEqualizer)
                SettingsDivider()
                SettingsToggleRow(
                    title = "Gapless playback",
                    description = "No silence between tracks on an album.",
                    checked = settings.gaplessPlayback,
                    onCheckedChange = viewModel::onGaplessChange,
                )
                SettingsDivider()
                SettingsToggleRow(
                    title = "Volume normalization",
                    description = "Evens out loudness between tracks. Off preserves original dynamics.",
                    checked = settings.volumeNormalization,
                    onCheckedChange = viewModel::onNormalizationChange,
                )
            }

            SettingsSection(title = "Discovery") {
                SettingsNavRow(
                    title = "Discovery Control",
                    value = uiState.discoveryMode.label,
                    onClick = onOpenDiscoveryControl,
                )
                SettingsDivider()
                SettingsToggleRow(
                    title = "Autoplay similar tracks",
                    description = "When your queue ends, keep playing. Off by default — your queue is yours.",
                    checked = settings.autoplaySimilar,
                    onCheckedChange = viewModel::onAutoplayChange,
                )
            }

            SettingsSection(title = "Privacy") {
                SettingsToggleRow(
                    title = "Private session",
                    description = "Nothing you play is added to your history or feeds the algorithm.",
                    checked = settings.privateSession,
                    onCheckedChange = viewModel::onPrivateSessionChange,
                )
                SettingsDivider()
                SettingsToggleRow(
                    title = "Notifications",
                    description = "Off by default. Silvery never sends promotional pushes.",
                    checked = settings.notifications,
                    onCheckedChange = viewModel::onNotificationsChange,
                )
            }

            SettingsSection(title = "Account") {
                SettingsNavRow(
                    title = "Profiles",
                    value = uiState.activeProfileName,
                    onClick = onOpenManageProfiles,
                )
                SettingsDivider()
                SettingsNavRow(
                    title = "Sign out",
                    onClick = viewModel::onSignOutRequested,
                )
            }

            SettingsSection(title = "About") {
                SettingsNavRow(
                    title = "How Silvery works",
                    onClick = onOpenHowItWorks,
                )
            }

            Text(
                // Read from the build so it can't drift from the released version.
                text = "Silvery · Version ${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.bodySmall,
                color = SilveryTheme.colors.textMuted,
                modifier = Modifier.padding(top = 32.dp),
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (uiState.showQualityPicker) {
        AudioQualityDialog(
            selected = settings.audioQuality,
            onSelect = viewModel::onQualitySelected,
            onDismiss = { viewModel.onQualityPickerVisibilityChange(false) },
        )
    }

    if (uiState.showSignOutConfirm) {
        SignOutDialog(
            onConfirm = {
                viewModel.onSignOutConfirmed()
                onSignedOut()
            },
            onDismiss = viewModel::onSignOutDismissed,
        )
    }
}

/** Signing out wipes saved data, so it asks first and says exactly what goes. */
@Composable
private fun SignOutDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SilveryTheme.colors.surfaceAlt,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = SilveryTheme.colors.textSecondary,
        title = { Text(text = "Sign out?") },
        text = {
            Text(
                text = "This clears the session saved on this device, resets your profiles " +
                    "and removes every liked song. You'll start again from onboarding.",
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = "Sign out", color = SilveryTheme.colors.liked)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel", color = MaterialTheme.colorScheme.onSurface)
            }
        },
    )
}

@Composable
private fun AudioQualityDialog(
    selected: AudioQuality,
    onSelect: (AudioQuality) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SilveryTheme.colors.surfaceAlt,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = SilveryTheme.colors.textSecondary,
        title = { Text(text = "Audio quality") },
        text = {
            Column {
                AudioQuality.entries.forEach { quality ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(quality) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = quality.label,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = quality.detail,
                                style = MaterialTheme.typography.bodySmall,
                                color = SilveryTheme.colors.textTertiary,
                            )
                        }
                        if (quality == selected) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Done", color = MaterialTheme.colorScheme.onSurface)
            }
        },
    )
}
