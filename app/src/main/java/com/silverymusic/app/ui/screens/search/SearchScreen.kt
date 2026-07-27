package com.silverymusic.app.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
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
import com.silverymusic.app.data.model.Genre
import com.silverymusic.app.theme.CardShape
import com.silverymusic.app.ui.components.DataStatePanel
import com.silverymusic.app.ui.components.SectionHeader
import com.silverymusic.app.ui.components.SilverySearchBar
import com.silverymusic.app.ui.components.TabScreenHeader
import com.silverymusic.app.ui.components.TrackRow
import com.silverymusic.app.ui.silveryViewModel

private val genreTileColors = listOf(
    Color(0xFF2E4A46), Color(0xFF3A2E4A), Color(0xFF4A3A2E),
    Color(0xFF2E3A4A), Color(0xFF44422C), Color(0xFF3E2E3A),
    Color(0xFF2E4A3A), Color(0xFF4A2E38),
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    onOpenProfileSwitcher: () -> Unit,
    viewModel: SearchViewModel = silveryViewModel { SearchViewModel(AppContainer.musicRepository) },
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        TabScreenHeader(
            profileName = uiState.profileName,
            title = "Search",
            onAvatarClick = onOpenProfileSwitcher,
        )
        SilverySearchBar(
            modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 16.dp),
            value = uiState.query,
            onValueChange = viewModel::onQueryChange,
            readOnly = false,
        )

        LazyColumn(
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item {
                DataStatePanel(
                    isLoading = uiState.isLoading,
                    error = uiState.error,
                    isEmpty = uiState.isEmpty,
                    emptyMessage = uiState.emptyMessage,
                    loadingMessage = if (uiState.isSearching) "Searching…" else "Loading…",
                    onRetry = viewModel::onRetry,
                )
            }
            if (uiState.filteredGenres.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionHeader(title = "Browse All")
                        FlowRow(
                            maxItemsInEachRow = 2,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth(),
                        ) {
                            uiState.filteredGenres.forEach { genre ->
                                GenreTile(
                                    genre = genre,
                                    color = genreTileColors[genre.id.hashCode().mod(genreTileColors.size)],
                                    selected = genre.id == uiState.browsingGenre?.id,
                                    onClick = {
                                        if (genre.id == uiState.browsingGenre?.id) {
                                            viewModel.onClearGenre()
                                        } else {
                                            viewModel.onGenreSelected(genre)
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                }
            }
            if (uiState.results.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = uiState.resultsTitle,
                        trailingText = if (uiState.isSearching) null else "See all",
                    )
                }
                itemsIndexed(uiState.results, key = { _, track -> track.id }) { index, track ->
                    TrackRow(
                        track = track,
                        onClick = { viewModel.onPlayResult(index) },
                        modifier = Modifier.padding(horizontal = 24.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun GenreTile(
    genre: Genre,
    color: Color,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(73.dp)
            .clip(CardShape)
            .background(color)
            .border(
                width = if (selected) 2.dp else 0.dp,
                color = if (selected) MaterialTheme.colorScheme.onBackground else Color.Transparent,
                shape = CardShape,
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = genre.name,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.padding(start = 16.dp),
        )
    }
}
