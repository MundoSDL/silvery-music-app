package com.silverymusic.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.silverymusic.app.data.AppContainer
import com.silverymusic.app.ui.components.ArtistCircle
import com.silverymusic.app.ui.components.DataStatePanel
import com.silverymusic.app.ui.components.FeaturedPlaylistCard
import com.silverymusic.app.ui.components.GenreChip
import com.silverymusic.app.ui.components.PlaylistTile
import com.silverymusic.app.ui.components.SectionHeader
import com.silverymusic.app.ui.components.SilverySearchBar
import com.silverymusic.app.ui.components.TabScreenHeader
import com.silverymusic.app.ui.silveryViewModel

@Composable
fun HomeScreen(
    onOpenSearch: () -> Unit,
    onOpenProfileSwitcher: () -> Unit,
    viewModel: HomeViewModel = silveryViewModel { HomeViewModel(AppContainer.musicRepository) },
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(bottom = 24.dp),
    ) {
        TabScreenHeader(
            profileName = uiState.profileName,
            title = "Home",
            onAvatarClick = onOpenProfileSwitcher,
        )
        SilverySearchBar(
            modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 16.dp),
            onClick = onOpenSearch,
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            item {
                DataStatePanel(
                    isLoading = uiState.isLoading,
                    error = uiState.error,
                    isEmpty = uiState.isEmpty,
                    emptyMessage = "Nothing in your home mix yet.",
                    onRetry = viewModel::onRetry,
                )
            }
            if (uiState.recentlyPlayed.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionHeader(title = "Recently Played", trailingText = "See all")
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            items(uiState.recentlyPlayed, key = { it.id }) { playlist ->
                                PlaylistTile(playlist = playlist, onClick = { viewModel.onPlayPlaylist(playlist) })
                            }
                        }
                    }
                }
            }
            if (uiState.madeForYou.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionHeader(title = "Made For You", trailingText = "See all")
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            items(uiState.madeForYou, key = { it.id }) { playlist ->
                                FeaturedPlaylistCard(playlist = playlist, onClick = { viewModel.onPlayPlaylist(playlist) })
                            }
                        }
                    }
                }
            }
            if (uiState.topGenres.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionHeader(title = "Your Top Genres")
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(uiState.topGenres, key = { it.id }) { genre ->
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
                                ArtistCircle(artist = artist, onClick = { viewModel.onPlayArtist(artist) })
                            }
                        }
                    }
                }
            }
        }
    }
}
