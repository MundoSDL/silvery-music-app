package com.silverymusic.app.ui.screens.equalizer

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.silverymusic.app.data.AppContainer
import com.silverymusic.app.data.model.EqPreset
import com.silverymusic.app.data.model.EqSettings
import com.silverymusic.app.theme.CardShape
import com.silverymusic.app.theme.PillShape
import com.silverymusic.app.theme.SilveryTheme
import com.silverymusic.app.ui.components.SilveryTopBar
import com.silverymusic.app.ui.silveryViewModel

@Composable
fun EqualizerScreen(
    onBack: () -> Unit,
    viewModel: EqualizerViewModel = silveryViewModel { EqualizerViewModel(AppContainer.musicRepository) },
) {
    val uiState by viewModel.uiState.collectAsState()
    val settings = uiState.settings

    // Bands fade rather than disappear when the EQ is off — the curve stays
    // readable so you can see what turning it back on would do.
    val bandsAlpha by animateFloatAsState(
        targetValue = if (settings.enabled) 1f else 0.4f,
        label = "eqBandsAlpha",
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        SilveryTopBar(
            title = "Equalizer",
            onBack = onBack,
            actionLabel = "Reset",
            actionEnabled = !uiState.isFlat,
            onActionClick = viewModel::onReset,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CardShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Equalizer",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = if (settings.enabled) "Shaping every track" else "Playing unprocessed audio",
                        style = MaterialTheme.typography.bodySmall,
                        color = SilveryTheme.colors.textTertiary,
                    )
                }
                Switch(
                    checked = settings.enabled,
                    onCheckedChange = viewModel::onEnabledChange,
                    colors = silverySwitchColors(),
                )
            }

            Text(
                text = "PRESETS",
                style = MaterialTheme.typography.labelMedium,
                color = SilveryTheme.colors.textTertiary,
                modifier = Modifier.padding(top = 28.dp, bottom = 12.dp),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                EqPreset.entries.forEach { preset ->
                    PresetChip(
                        label = preset.label,
                        selected = preset == settings.preset,
                        // Custom isn't something you pick — it's what the EQ becomes
                        // once you drag a band, so it's shown but not selectable.
                        enabled = preset != EqPreset.CUSTOM,
                        onClick = { viewModel.onPresetSelected(preset) },
                    )
                }
            }

            Text(
                text = "BANDS",
                style = MaterialTheme.typography.labelMedium,
                color = SilveryTheme.colors.textTertiary,
                modifier = Modifier.padding(top = 28.dp, bottom = 4.dp),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(bandsAlpha)
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(
                    modifier = Modifier.padding(top = 22.dp, end = 4.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End,
                ) {
                    Text(text = "+12", style = MaterialTheme.typography.labelSmall, color = SilveryTheme.colors.textMuted)
                    Spacer(modifier = Modifier.height(70.dp))
                    Text(text = "0", style = MaterialTheme.typography.labelSmall, color = SilveryTheme.colors.textMuted)
                    Spacer(modifier = Modifier.height(70.dp))
                    Text(text = "−12", style = MaterialTheme.typography.labelSmall, color = SilveryTheme.colors.textMuted)
                }

                EqSettings.BAND_LABELS.forEachIndexed { index, label ->
                    EqBandSlider(
                        label = label,
                        gainDb = settings.gains.getOrElse(index) { 0f },
                        enabled = settings.enabled,
                        onGainChange = { viewModel.onBandChange(index, it) },
                    )
                }
            }

            Text(
                text = "Drag a band to shape the curve. Changes apply instantly and stay with this profile.",
                style = MaterialTheme.typography.bodySmall,
                color = SilveryTheme.colors.textTertiary,
                modifier = Modifier.padding(top = 24.dp),
            )
        }
    }
}

@Composable
private fun PresetChip(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        color = when {
            selected -> MaterialTheme.colorScheme.background
            enabled -> MaterialTheme.colorScheme.onSurface
            else -> SilveryTheme.colors.textMuted
        },
        modifier = modifier
            .clip(PillShape)
            .background(
                if (selected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.surface,
            )
            .border(
                width = 1.dp,
                color = if (selected) MaterialTheme.colorScheme.onBackground else SilveryTheme.colors.border,
                shape = PillShape,
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
internal fun silverySwitchColors() = SwitchDefaults.colors(
    checkedThumbColor = MaterialTheme.colorScheme.background,
    checkedTrackColor = MaterialTheme.colorScheme.onBackground,
    checkedBorderColor = MaterialTheme.colorScheme.onBackground,
    uncheckedThumbColor = SilveryTheme.colors.textTertiary,
    uncheckedTrackColor = SilveryTheme.colors.surfaceAlt,
    uncheckedBorderColor = SilveryTheme.colors.border,
)
