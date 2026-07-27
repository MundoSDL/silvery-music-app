package com.silverymusic.app.ui.screens.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.silverymusic.app.data.AppContainer
import com.silverymusic.app.data.model.Artist
import com.silverymusic.app.data.model.Playlist
import com.silverymusic.app.theme.PillShape
import com.silverymusic.app.theme.SilveryTheme
import com.silverymusic.app.ui.components.ArtistCircle
import com.silverymusic.app.ui.components.Artwork
import com.silverymusic.app.ui.components.DataStatePanel
import com.silverymusic.app.ui.components.SilverySearchBar
import com.silverymusic.app.ui.components.TabScreenHeader
import com.silverymusic.app.ui.silveryViewModel

@Composable
fun LibraryScreen(
    onOpenSearch: () -> Unit,
    onOpenProfileSwitcher: () -> Unit,
    viewModel: LibraryViewModel = silveryViewModel { LibraryViewModel(AppContainer.musicRepository) },
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        TabScreenHeader(
            profileName = uiState.profileName,
            title = "Library",
            onAvatarClick = onOpenProfileSwitcher,
        )
        SilverySearchBar(
            modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 16.dp),
            onClick = onOpenSearch,
        )

        Row(
            modifier = Modifier.padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            LibraryTab.entries.forEach { tab ->
                val selected = tab == uiState.selectedTab
                Text(
                    text = tab.label,
                    style = MaterialTheme.typography.labelLarge,
                    // The selected pill is a light surface, so its label goes dark.
                    color = if (selected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .clip(PillShape)
                        .let { if (selected) it.background(MaterialTheme.colorScheme.onBackground) else it }
                        .clickable(onClick = { viewModel.onTabSelected(tab) })
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Recently Added",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            )
        }

        DataStatePanel(
            isLoading = uiState.isLoading,
            error = uiState.error,
            isEmpty = uiState.isSelectedTabEmpty,
            emptyMessage = "Nothing saved in ${uiState.selectedTab.label.lowercase()} yet.",
            onRetry = viewModel::onRetry,
        )

        LazyColumn(contentPadding = PaddingValues(horizontal = 24.dp, vertical = 4.dp)) {
            when (uiState.selectedTab) {
                LibraryTab.PLAYLISTS -> items(uiState.playlists, key = { it.id }) { playlist ->
                    LibraryPlaylistRow(playlist, onClick = { viewModel.onPlayPlaylist(playlist) })
                }
                LibraryTab.ALBUMS -> items(uiState.albums, key = { it.id }) { playlist ->
                    LibraryPlaylistRow(playlist, onClick = { viewModel.onPlayPlaylist(playlist) })
                }
                LibraryTab.RADIO -> items(uiState.radio, key = { it.id }) { playlist ->
                    LibraryPlaylistRow(playlist, onClick = { viewModel.onPlayPlaylist(playlist) })
                }
                LibraryTab.ARTISTS -> items(uiState.artists, key = { it.id }) { artist ->
                    LibraryArtistRow(artist, onClick = { viewModel.onPlayArtist(artist) })
                }
            }
        }
    }
}

@Composable
private fun LibraryPlaylistRow(
    playlist: Playlist,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Artwork(
                url = playlist.artworkUrl,
                contentDescription = null,
                placeholder = SolidColor(SilveryTheme.colors.surfaceAlt),
                modifier = Modifier.size(49.dp),
            )
            if (playlist.artworkUrl.isNullOrBlank()) {
                Icon(
                    imageVector = Icons.Filled.MusicNote,
                    contentDescription = null,
                    tint = SilveryTheme.colors.textTertiary,
                )
            }
        }
        Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
            Text(text = playlist.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), maxLines = 1)
            Text(text = playlist.subtitle, style = MaterialTheme.typography.bodySmall, color = SilveryTheme.colors.textTertiary, maxLines = 1)
        }
        Icon(imageVector = Icons.Filled.MoreVert, contentDescription = "More", tint = SilveryTheme.colors.textTertiary)
    }
}

@Composable
private fun LibraryArtistRow(
    artist: Artist,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ArtistCircle(artist = artist, size = 49.dp)
        Text(
            text = artist.name,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.padding(start = 12.dp),
        )
    }
}
