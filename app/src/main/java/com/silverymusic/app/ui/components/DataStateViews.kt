package com.silverymusic.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.silverymusic.app.data.DataError
import com.silverymusic.app.theme.SilveryTheme
import com.silverymusic.app.ui.motion.LocalReducedMotion

/** Plain-language wording for every failure the repository can hand the UI. */
fun DataError.userMessage(): String = when (this) {
    DataError.NotConfigured ->
        "The Jamendo API key isn't configured yet, so there's nothing to load."
    DataError.Network -> "No connection. The catalog is out of reach right now."
    DataError.Timeout -> "The catalog took too long to answer."
    is DataError.Http -> "The catalog answered with an error ($code)."
    is DataError.Unknown -> message ?: "Something went wrong loading this."
}

/** A missing API key is a setup step, not a transient failure — retry can't fix it. */
private fun DataError.isRetryable(): Boolean = this != DataError.NotConfigured

/**
 * One quiet block covering loading, failure and empty. Renders nothing when
 * there is real content to show, so screens can place it unconditionally.
 */
@Composable
fun DataStatePanel(
    isLoading: Boolean,
    error: DataError?,
    isEmpty: Boolean,
    modifier: Modifier = Modifier,
    emptyMessage: String = "Nothing here yet.",
    loadingMessage: String = "Loading…",
    onRetry: () -> Unit = {},
    /** Placeholder rows under the loading message; 0 for panels that aren't lists. */
    skeletonRows: Int = 3,
) {
    if (!isLoading && error == null && !isEmpty) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
    ) {
        when {
            isLoading -> {
                // Screen readers still hear what is loading; sighted users get
                // calm placeholder rows in the shape of what is coming.
                Text(
                    text = loadingMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = SilveryTheme.colors.textTertiary,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
                if (skeletonRows > 0) SkeletonRows(count = skeletonRows)
            }

            error != null -> {
                Text(
                    text = error.userMessage(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = SilveryTheme.colors.textSecondary,
                )
                if (error.isRetryable()) {
                    Text(
                        text = "Try again",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier
                            .padding(top = 10.dp)
                            .clickable(onClick = onRetry),
                    )
                }
            }

            else -> Text(
                text = emptyMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = SilveryTheme.colors.textTertiary,
            )
        }
    }
}

/** Placeholder rows with a slow light sweep; static when reduced motion is on. */
@Composable
fun SkeletonRows(modifier: Modifier = Modifier, count: Int = 3) {
    val shimmer = rememberShimmerProgress()
    val base = SilveryTheme.colors.surfaceAlt
    val highlight = SilveryTheme.colors.artPlaceholder.copy(alpha = 0.6f)
    val block = Modifier.drawWithCache {
        onDrawBehind {
            val width = size.width
            val sweep = width * 1.5f
            val x = -sweep + (width + sweep * 2f) * shimmer.value
            drawRect(base)
            drawRect(
                Brush.linearGradient(
                    colors = listOf(Color.Transparent, highlight, Color.Transparent),
                    start = Offset(x, 0f),
                    end = Offset(x + sweep, size.height),
                ),
            )
        }
    }
    Column(modifier = modifier.fillMaxWidth()) {
        repeat(count) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp),
            ) {
                Box(modifier = Modifier.size(width = 41.dp, height = 38.dp).clip(RoundedCornerShape(4.dp)).then(block))
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Spacer(modifier = Modifier.size(width = 140.dp, height = 12.dp).clip(RoundedCornerShape(3.dp)).then(block))
                    Spacer(modifier = Modifier.height(8.dp))
                    Spacer(modifier = Modifier.size(width = 90.dp, height = 10.dp).clip(RoundedCornerShape(3.dp)).then(block))
                }
            }
        }
    }
}

@Composable
private fun rememberShimmerProgress(): State<Float> {
    if (LocalReducedMotion.current) return remember { mutableFloatStateOf(0.5f) }
    return rememberInfiniteTransition(label = "skeleton").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 1_400, easing = LinearEasing), RepeatMode.Restart),
        label = "skeletonSweep",
    )
}
