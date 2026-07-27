package com.silverymusic.app.ui.screens.profiles

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.silverymusic.app.data.AppContainer
import com.silverymusic.app.theme.CardShape
import com.silverymusic.app.theme.ProfileAccents
import com.silverymusic.app.theme.SearchBarShape
import com.silverymusic.app.theme.SilveryTheme
import com.silverymusic.app.ui.components.ProfileAvatar
import com.silverymusic.app.ui.components.SilveryTopBar
import com.silverymusic.app.ui.screens.equalizer.silverySwitchColors
import com.silverymusic.app.ui.silveryViewModel

@Composable
fun AddProfileScreen(
    onBack: () -> Unit,
    onCreated: () -> Unit,
    viewModel: AddProfileViewModel = silveryViewModel { AddProfileViewModel(AppContainer.musicRepository) },
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        SilveryTopBar(title = "Add Profile", onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                ProfileAvatar(
                    name = uiState.name.ifBlank { "?" },
                    accentIndex = uiState.accentIndex,
                    size = 88.dp,
                )
            }

            Text(
                text = "NAME",
                style = MaterialTheme.typography.labelMedium,
                color = SilveryTheme.colors.textTertiary,
                modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
            )
            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChange,
                placeholder = { Text(text = "Profile name", color = SilveryTheme.colors.textMuted) },
                singleLine = true,
                shape = CardShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    cursorColor = MaterialTheme.colorScheme.onSurface,
                    focusedBorderColor = SilveryTheme.colors.border,
                    unfocusedBorderColor = Color.Transparent,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            Text(
                text = "COLOUR",
                style = MaterialTheme.typography.labelMedium,
                color = SilveryTheme.colors.textTertiary,
                modifier = Modifier.padding(top = 28.dp, bottom = 12.dp),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ProfileAccents.forEachIndexed { index, color ->
                    AccentSwatch(
                        color = color,
                        selected = index == uiState.accentIndex,
                        onClick = { viewModel.onAccentSelected(index) },
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 28.dp)
                    .clip(CardShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                    Text(
                        text = "Kid profile",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "Filters explicit tracks, and keeps this listening out of the main account's algorithm.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SilveryTheme.colors.textTertiary,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                Switch(
                    checked = uiState.isKid,
                    onCheckedChange = viewModel::onKidChange,
                    colors = silverySwitchColors(),
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        Button(
            onClick = {
                viewModel.onCreate()
                onCreated()
            },
            enabled = uiState.canCreate,
            shape = SearchBarShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.onBackground,
                contentColor = MaterialTheme.colorScheme.background,
                disabledContainerColor = SilveryTheme.colors.surfaceAlt,
                disabledContentColor = SilveryTheme.colors.textMuted,
            ),
            contentPadding = PaddingValues(vertical = 16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
                .height(52.dp),
        ) {
            Text(text = "Create Profile", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun AccentSwatch(
    color: Color,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (selected) 2.dp else 0.dp,
                color = if (selected) MaterialTheme.colorScheme.onBackground else Color.Transparent,
                shape = CircleShape,
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
