package com.silverymusic.app.ui.screens.seeall

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.silverymusic.app.data.AppContainer
import com.silverymusic.app.data.model.Artist
import com.silverymusic.app.data.model.Playlist
import com.silverymusic.app.theme.SilveryTheme
import com.silverymusic.app.ui.components.ArtistCircle
import com.silverymusic.app.ui.components.Artwork
import com.silverymusic.app.ui.components.DataStatePanel
import com.silverymusic.app.ui.components.GenreChip
import com.silverymusic.app.ui.components.SilveryTopBar
import com.silverymusic.app.ui.silveryViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SeeAllScreen(
    section: SeeAllSection,
    onBack: () -> Unit,
    viewModel: SeeAllViewModel = silveryViewModel {
        SeeAllViewModel(AppContainer.musicRepository, section)
    },
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        SilveryTopBar(title = uiState.title, onBack = onBack)

        DataStatePanel(
            isLoading = uiState.isLoading,
            error = uiState.error,
            isEmpty = uiState.isEmpty,
            emptyMessage = "Nothing here yet.",
            onRetry = viewModel::onRetry,
        )

        LazyColumn(contentPadding = PaddingValues(horizontal = 24.dp, vertical = 4.dp)) {
            if (uiState.playlists.isNotEmpty()) {
                items(uiState.playlists, key = { it.id }) { playlist ->
                    PlaylistRow(playlist, onClick = { viewModel.onPlayPlaylist(playlist) })
                }
            }
            if (uiState.artists.isNotEmpty()) {
                items(uiState.artists, key = { it.id }) { artist ->
                    ArtistRow(artist, onClick = { viewModel.onPlayArtist(artist) })
                }
            }
            if (uiState.genres.isNotEmpty()) {
                item {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        uiState.genres.forEach { genre ->
                            GenreChip(label = genre.name, onClick = { viewModel.onPlayGenre(genre) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaylistRow(
    playlist: Playlist,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
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
private fun ArtistRow(
    artist: Artist,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
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
