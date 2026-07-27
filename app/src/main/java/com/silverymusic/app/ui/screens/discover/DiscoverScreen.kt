package com.silverymusic.app.ui.screens.discover

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
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
import androidx.compose.ui.unit.dp
import com.silverymusic.app.data.AppContainer
import com.silverymusic.app.theme.CardShape
import com.silverymusic.app.theme.PillShape
import com.silverymusic.app.theme.SilveryTheme
import com.silverymusic.app.ui.components.ArtistCircle
import com.silverymusic.app.ui.components.DataStatePanel
import com.silverymusic.app.ui.components.GenreChip
import com.silverymusic.app.ui.components.SectionHeader
import com.silverymusic.app.ui.components.SilverySearchBar
import com.silverymusic.app.ui.components.TabScreenHeader
import com.silverymusic.app.ui.components.TrackRow
import com.silverymusic.app.ui.silveryViewModel

@Composable
fun DiscoverScreen(
    onOpenSearch: () -> Unit,
    onOpenProfileSwitcher: () -> Unit,
    onOpenDiscoveryControl: () -> Unit,
    viewModel: DiscoverViewModel = silveryViewModel { DiscoverViewModel(AppContainer.musicRepository) },
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        TabScreenHeader(
            profileName = uiState.profileName,
            title = "Discover",
            onAvatarClick = onOpenProfileSwitcher,
        )
        SilverySearchBar(
            modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 16.dp),
            onClick = onOpenSearch,
        )

        LazyColumn(
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            item {
                DataStatePanel(
                    isLoading = uiState.isLoading,
                    error = uiState.error,
                    isEmpty = uiState.isEmpty,
                    emptyMessage = "No discoveries waiting right now.",
                    onRetry = viewModel::onRetry,
                )
            }
            uiState.currentVibe?.let { vibe ->
                item {
                    CurrentVibeCard(
                        title = vibe.title,
                        subtitle = vibe.subtitle,
                        canPlay = uiState.canPlayMix,
                        onPlay = viewModel::onPlayMix,
                        onShuffle = viewModel::onShuffleMix,
                        modifier = Modifier.padding(horizontal = 24.dp),
                    )
                }
            }
            if (uiState.queueTracks.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        SectionHeader(
                            title = "Discovery Mix",
                            trailingText = "${uiState.discoveryMode.label} ›",
                            onTrailingClick = onOpenDiscoveryControl,
                        )
                        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                            uiState.queueTracks.forEachIndexed { index, track ->
                                TrackRow(
                                    track = track,
                                    onClick = { viewModel.onPlayTrack(index) },
                                    onLikeClick = { viewModel.onToggleLike(track.id) },
                                )
                            }
                        }
                    }
                }
            }
            if (uiState.browseGenres.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionHeader(title = "Browse Genres", trailingText = "See all")
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(uiState.browseGenres, key = { it.id }) { genre ->
                                GenreChip(label = genre.name, onClick = { viewModel.onPlayGenre(genre) })
                            }
                        }
                    }
                }
            }
            if (uiState.yourArtists.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionHeader(title = "Your Artists", trailingText = "See all")
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            items(uiState.yourArtists, key = { it.id }) { artist ->
                                ArtistCircle(
                                    artist = artist,
                                    onClick = { viewModel.onPlayArtist(artist) },
                                    modifier = Modifier.padding(2.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CurrentVibeCard(
    title: String,
    subtitle: String,
    canPlay: Boolean,
    onPlay: () -> Unit,
    onShuffle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.surface)
            .padding(20.dp),
    ) {
        Text(
            text = "CURRENT VIBE",
            style = MaterialTheme.typography.labelMedium,
            color = SilveryTheme.colors.textTertiary,
        )
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(top = 4.dp),
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = SilveryTheme.colors.textTertiary,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(PillShape)
                    .background(MaterialTheme.colorScheme.onBackground)
                    .clickable(enabled = canPlay, onClick = onPlay)
                    .padding(horizontal = 20.dp, vertical = 10.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    // On the light pill — must stay dark.
                    tint = MaterialTheme.colorScheme.background,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "Play",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.padding(start = 6.dp),
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(PillShape)
                    .background(SilveryTheme.colors.surfaceAlt)
                    .border(1.dp, SilveryTheme.colors.border, PillShape)
                    .clickable(enabled = canPlay, onClick = onShuffle)
                    .padding(horizontal = 20.dp, vertical = 10.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Shuffle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "Shuffle",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(start = 6.dp),
                )
            }
        }
    }
}
