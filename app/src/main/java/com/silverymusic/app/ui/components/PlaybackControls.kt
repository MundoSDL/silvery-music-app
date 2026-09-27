package com.silverymusic.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.silverymusic.app.theme.SilveryTheme
import com.silverymusic.app.ui.motion.LocalReducedMotion
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.launch

private const val BURST_DOTS = 8

/**
 * The heart used wherever a track can be liked. Liking pops the icon with a
 * springy bounce, throws a small ring of sparks and gives a haptic tick;
 * unliking just settles. The button keeps the full 48dp touch target.
 */
@Composable
fun LikeButton(
    liked: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    iconSize: Dp = 24.dp,
    inactiveTint: Color = MaterialTheme.colorScheme.onSurface,
) {
    val reduced = LocalReducedMotion.current
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val scale = remember { Animatable(1f) }
    // 1f means "no burst showing"; a like runs it from 0f back up to 1f.
    val burst = remember { Animatable(1f) }
    val likedColor = SilveryTheme.colors.liked
    val tint by animateColorAsState(
        targetValue = if (liked) likedColor else inactiveTint,
        animationSpec = tween(durationMillis = if (reduced) 0 else 200),
        label = "likeTint",
    )

    IconButton(
        onClick = {
            val willLike = !liked
            onToggle()
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            if (!reduced) {
                scope.launch {
                    scale.snapTo(if (willLike) 0.6f else 0.85f)
                    scale.animateTo(
                        targetValue = 1f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                    )
                }
            }
            if (willLike && !reduced) {
                scope.launch {
                    burst.snapTo(0f)
                    burst.animateTo(1f, tween(durationMillis = 450, easing = FastOutSlowInEasing))
                }
            }
        },
        enabled = enabled,
        modifier = modifier,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(iconSize)
                .drawBehind {
                    val progress = burst.value
                    if (progress >= 1f) return@drawBehind
                    val maxRadius = iconSize.toPx() * 0.85f
                    val ringRadius = maxRadius * (0.45f + 0.55f * progress)
                    val dotRadius = 2.dp.toPx() * (1f - progress)
                    for (i in 0 until BURST_DOTS) {
                        val angle = (2 * PI * i / BURST_DOTS).toFloat()
                        drawCircle(
                            color = likedColor.copy(alpha = 1f - progress),
                            radius = dotRadius,
                            center = center + Offset(cos(angle) * ringRadius, sin(angle) * ringRadius),
                        )
                    }
                },
        ) {
            Icon(
                imageVector = if (liked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = if (liked) "Unlike" else "Like",
                tint = tint,
                modifier = Modifier
                    .size(iconSize)
                    .graphicsLayer {
                        scaleX = scale.value
                        scaleY = scale.value
                    },
            )
        }
    }
}

/** Play and pause swap with a quick scale-and-fade instead of a hard cut. */
@Composable
fun PlayPauseIcon(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    size: Dp = 24.dp,
) {
    val reduced = LocalReducedMotion.current
    AnimatedContent(
        targetState = isPlaying,
        transitionSpec = {
            if (reduced) {
                EnterTransition.None togetherWith ExitTransition.None
            } else {
                (scaleIn(initialScale = 0.5f, animationSpec = tween(180)) + fadeIn(tween(180))) togetherWith
                    (scaleOut(targetScale = 0.5f, animationSpec = tween(140)) + fadeOut(tween(140)))
            }
        },
        contentAlignment = Alignment.Center,
        modifier = modifier,
        label = "playPause",
    ) { playing ->
        Icon(
            imageVector = if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
            contentDescription = if (playing) "Pause" else "Play",
            tint = tint,
            modifier = Modifier.size(size),
        )
    }
}
