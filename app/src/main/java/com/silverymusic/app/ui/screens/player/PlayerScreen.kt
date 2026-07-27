package com.silverymusic.app.ui.screens.player

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Subject
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import com.silverymusic.app.data.AppContainer
import com.silverymusic.app.data.DataError
import com.silverymusic.app.data.model.ListeningStatus
import com.silverymusic.app.data.model.Lyrics
import com.silverymusic.app.data.model.NowPlaying
import com.silverymusic.app.data.model.formatDuration
import com.silverymusic.app.theme.SilveryTheme
import com.silverymusic.app.ui.components.Artwork
import com.silverymusic.app.ui.components.DataStatePanel
import com.silverymusic.app.ui.silveryViewModel

@Composable
fun PlayerScreen(
    onMinimize: () -> Unit,
    onOpenSync: () -> Unit,
    onOpenEq: () -> Unit,
    onOpenQueue: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: PlayerViewModel = silveryViewModel {
        PlayerViewModel(AppContainer.musicRepository, AppContainer.lyricsRepository)
    },
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    LaunchedEffect(Unit) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collect { effect ->
                when (effect) {
                    is PlayerEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        val nowPlaying = uiState.nowPlaying
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (nowPlaying != null) {
                PlayerContent(
                    nowPlaying = nowPlaying,
                    uiState = uiState,
                    onMinimize = onMinimize,
                    onOpenSync = onOpenSync,
                    onTogglePlayPause = viewModel::onTogglePlayPause,
                    onSkipNext = viewModel::onSkipNext,
                    onSkipPrevious = viewModel::onSkipPrevious,
                    onSeek = viewModel::onSeek,
                    onToggleLyrics = viewModel::onToggleLyrics,
                    onRetryLyrics = viewModel::onRetryLyrics,
                    onOpenEq = onOpenEq,
                    onOpenQueue = onOpenQueue,
                    onFeelingLucky = viewModel::onFeelingLucky,
                    onOpenSettings = onOpenSettings,
                )
            }
        }
    }
}

@Composable
private fun PlayerContent(
    nowPlaying: NowPlaying,
    uiState: PlayerUiState,
    onMinimize: () -> Unit,
    onOpenSync: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSeek: (Float) -> Unit,
    onToggleLyrics: () -> Unit,
    onRetryLyrics: () -> Unit,
    onOpenEq: () -> Unit,
    onOpenQueue: () -> Unit,
    onFeelingLucky: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    // Swipe-down-to-dismiss. The sheet follows the finger and fades as it goes;
    // past the threshold it commits to a minimize, otherwise it springs back.
    val scope = rememberCoroutineScope()
    val dragOffset = remember { Animatable(0f) }
    val dismissThresholdPx = with(LocalDensity.current) { 160.dp.toPx() }
    val dragProgress = (dragOffset.value / dismissThresholdPx).coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .offset { IntOffset(0, dragOffset.value.roundToInt()) }
            .alpha(1f - dragProgress * 0.35f)
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onVerticalDrag = { change, dragAmount ->
                        // Only downward pulls move the sheet; guard so an upward
                        // flick can't drag it off the top of the screen.
                        val target = (dragOffset.value + dragAmount).coerceAtLeast(0f)
                        change.consume()
                        scope.launch { dragOffset.snapTo(target) }
                    },
                    onDragEnd = {
                        if (dragOffset.value > dismissThresholdPx) {
                            onMinimize()
                        } else {
                            scope.launch { dragOffset.animateTo(0f) }
                        }
                    },
                    onDragCancel = { scope.launch { dragOffset.animateTo(0f) } },
                )
            },
    ) {
        DragHandle(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 8.dp, bottom = 2.dp),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onMinimize) {
                Icon(imageVector = Icons.Filled.KeyboardArrowDown, contentDescription = "Minimize")
            }
            Text(
                text = "Playing from ${nowPlaying.sourceLabel}",
                style = MaterialTheme.typography.bodyMedium,
                color = SilveryTheme.colors.textTertiary,
            )
            IconButton(onClick = onOpenSettings) {
                Icon(imageVector = Icons.Filled.MoreVert, contentDescription = "More")
            }
        }

        ListeningStatusPill(
            status = nowPlaying.listeningStatus,
            onClick = onOpenSync,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 8.dp, bottom = 20.dp),
        )

        // The stage keeps its slot in the column whichever face is showing, so
        // switching to lyrics never moves the transport controls under the thumb.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 32.dp),
            contentAlignment = Alignment.Center,
        ) {
            Crossfade(
                targetState = uiState.showLyrics,
                animationSpec = tween(durationMillis = 320),
                label = "playerStage",
            ) { showLyrics ->
                if (showLyrics) {
                    LyricsPane(
                        lyrics = uiState.lyrics,
                        isLoading = uiState.isLoadingLyrics,
                        error = uiState.lyricsError,
                        isEmpty = uiState.lyricsAreEmpty,
                        positionMs = nowPlaying.positionMs,
                        onRetry = onRetryLyrics,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Artwork(
                            url = nowPlaying.track.artworkUrl,
                            contentDescription = "${nowPlaying.track.title} cover art",
                            shape = MaterialTheme.shapes.large,
                            placeholder = Brush.verticalGradient(
                                listOf(
                                    SilveryTheme.colors.artPlaceholder,
                                    MaterialTheme.colorScheme.background,
                                ),
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f),
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 24.dp),
        ) {
            Text(
                text = nowPlaying.track.title,
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold),
            )
            Text(
                text = nowPlaying.track.artist,
                style = MaterialTheme.typography.bodyLarge,
                color = SilveryTheme.colors.textTertiary,
                modifier = Modifier.padding(top = 2.dp),
            )

            // While the thumb is held the slider follows the finger; live position
            // only takes over again once the seek has been committed.
            var dragFraction by remember { mutableStateOf<Float?>(null) }
            Slider(
                value = dragFraction ?: nowPlaying.positionFraction,
                onValueChange = { dragFraction = it },
                onValueChangeFinished = {
                    dragFraction?.let(onSeek)
                    dragFraction = null
                },
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.onBackground,
                    activeTrackColor = MaterialTheme.colorScheme.onBackground,
                    inactiveTrackColor = SilveryTheme.colors.surfaceAlt,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                val elapsed = dragFraction
                    ?.let { formatDuration((it * nowPlaying.durationMs).toLong()) }
                    ?: nowPlaying.elapsedLabel
                Text(
                    text = elapsed,
                    style = MaterialTheme.typography.bodySmall,
                    color = SilveryTheme.colors.textTertiary,
                )
                Text(
                    text = if (nowPlaying.isBuffering) "Buffering…" else nowPlaying.remainingLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = SilveryTheme.colors.textTertiary,
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onSkipPrevious) {
                    Icon(imageVector = Icons.Filled.SkipPrevious, contentDescription = "Previous", modifier = Modifier.size(32.dp))
                }
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onBackground)
                        .clickable(onClick = onTogglePlayPause),
                    contentAlignment = Alignment.Center,
                ) {
                    if (nowPlaying.isBuffering) {
                        // Sits on the light play button, so it has to be dark.
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.background,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(26.dp),
                        )
                    } else {
                        Icon(
                            imageVector = if (nowPlaying.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (nowPlaying.isPlaying) "Pause" else "Play",
                            tint = MaterialTheme.colorScheme.background,
                            modifier = Modifier.size(32.dp),
                        )
                    }
                }
                IconButton(onClick = onSkipNext) {
                    Icon(imageVector = Icons.Filled.SkipNext, contentDescription = "Next", modifier = Modifier.size(32.dp))
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 28.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                PlayerActionButton(
                    icon = Icons.Filled.Subject,
                    label = "Lyrics",
                    onClick = onToggleLyrics,
                    active = uiState.showLyrics,
                )
                PlayerActionButton(icon = Icons.Filled.QueueMusic, label = "Queue", onClick = onOpenQueue)
                PlayerActionButton(icon = Icons.Filled.GraphicEq, label = "EQ", onClick = onOpenEq)
                PlayerActionButton(icon = Icons.Filled.Casino, label = "I'm Feeling Lucky", onClick = onFeelingLucky)
            }
        }
    }
}

@Composable
private fun DragHandle(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(36.dp)
            .height(4.dp)
            .clip(CircleShape)
            .background(SilveryTheme.colors.textMuted.copy(alpha = 0.5f)),
    )
}

@Composable
private fun LyricsPane(
    lyrics: Lyrics?,
    isLoading: Boolean,
    error: DataError?,
    isEmpty: Boolean,
    positionMs: Long,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (lyrics != null && !lyrics.isEmpty) {
            if (lyrics.isSynced) {
                SyncedLyrics(lyrics = lyrics, positionMs = positionMs)
            } else {
                PlainLyrics(lyrics = lyrics)
            }
        } else {
            DataStatePanel(
                isLoading = isLoading,
                error = error,
                isEmpty = isEmpty,
                emptyMessage = "No lyrics for this track.",
                loadingMessage = "Finding lyrics…",
                onRetry = onRetry,
            )
        }
    }
}

@Composable
private fun SyncedLyrics(lyrics: Lyrics, positionMs: Long) {
    val listState = rememberLazyListState()
    val activeIndex = lyrics.activeLineIndex(positionMs)

    LaunchedEffect(activeIndex, lyrics.trackId) {
        if (activeIndex >= 0) {
            // Park the active line a third of the way down instead of at the very
            // top, so the lines about to be sung stay in view.
            val offset = -(listState.layoutInfo.viewportSize.height / 3)
            listState.animateScrollToItem(activeIndex, offset)
        }
    }

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        itemsIndexed(lyrics.lines) { index, line ->
            val isActive = index == activeIndex
            val color by animateColorAsState(
                targetValue = if (isActive) {
                    MaterialTheme.colorScheme.onBackground
                } else {
                    SilveryTheme.colors.textMuted
                },
                animationSpec = tween(durationMillis = 400),
                label = "lyricLine",
            )
            Text(
                text = line.text,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 19.sp,
                    lineHeight = 26.sp,
                    fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                ),
                color = color,
            )
        }
    }
}

@Composable
private fun PlainLyrics(lyrics: Lyrics) {
    LazyColumn(
        contentPadding = PaddingValues(vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            Text(
                text = "Unsynced lyrics",
                style = MaterialTheme.typography.labelMedium,
                color = SilveryTheme.colors.textTertiary,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }
        // No timings to trust, so nothing gets highlighted — a calm block instead.
        items(lyrics.lines.size) { index ->
            Text(
                text = lyrics.lines[index].text,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 19.sp, lineHeight = 26.sp),
                color = SilveryTheme.colors.textSecondary,
            )
        }
    }
}

@Composable
private fun ListeningStatusPill(status: ListeningStatus, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when (status) {
            is ListeningStatus.Solo -> {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = SilveryTheme.colors.textTertiary,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = "Solo",
                    style = MaterialTheme.typography.labelLarge,
                    color = SilveryTheme.colors.textTertiary,
                    modifier = Modifier.padding(start = 6.dp),
                )
            }
            is ListeningStatus.Synced -> {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(SilveryTheme.colors.liveDot),
                )
                Text(
                    text = "Synced with ${status.friendName}",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun PlayerActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    active: Boolean = false,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onClick)) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(if (active) MaterialTheme.colorScheme.surface else Color.Transparent)
                .padding(6.dp),
        ) {
            Icon(imageVector = icon, contentDescription = label)
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (active) MaterialTheme.colorScheme.onBackground else SilveryTheme.colors.textTertiary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}
