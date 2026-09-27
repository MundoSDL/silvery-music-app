package com.silverymusic.app.ui.motion

import android.graphics.Bitmap
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * True when the user has switched animations off system-wide (Developer options
 * or Accessibility "Remove animations"). Decorative motion checks this and
 * falls back to an instant change.
 */
val LocalReducedMotion = staticCompositionLocalOf { false }

@Composable
fun rememberReducedMotion(): Boolean {
    val context = LocalContext.current
    return remember {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
}

// ---- Shared element plumbing ------------------------------------------------

/** Provided by [com.silverymusic.app.navigation.SilveryApp]; null in previews. */
@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = staticCompositionLocalOf<SharedTransitionScope?> { null }

/** The enter/exit scope of whatever hosts the element (a nav destination or the mini bar). */
val LocalNavAnimatedVisibilityScope = staticCompositionLocalOf<AnimatedVisibilityScope?> { null }

/** Key for the cover art that flies between the mini player and the full player. */
const val SharedArtworkKey = "now-playing-artwork"

/**
 * Tags this element as the one that morphs between the mini player and the full
 * player. A no-op wherever the scopes aren't provided (previews, tests) or when
 * the user has asked for reduced motion.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.sharedPlayerElement(key: String): Modifier {
    val sharedScope = LocalSharedTransitionScope.current ?: return this
    val visibilityScope = LocalNavAnimatedVisibilityScope.current ?: return this
    if (LocalReducedMotion.current) return this
    return with(sharedScope) {
        this@sharedPlayerElement.sharedElement(
            state = rememberSharedContentState(key = key),
            animatedVisibilityScope = visibilityScope,
            boundsTransform = { _, _ ->
                spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)
            },
        )
    }
}

// ---- Playback position --------------------------------------------------------

/**
 * The controller reports position about twice a second. This extrapolates
 * between reports on every frame while playing, so progress bars glide and
 * lyric fills sweep instead of stepping. Read the returned state in a draw or
 * layout lambda where possible, so only that phase re-runs each frame.
 */
@Composable
fun rememberSmoothPositionMs(positionMs: Long, isPlaying: Boolean, durationMs: Long): State<Long> {
    val reduced = LocalReducedMotion.current
    val smooth = remember { mutableLongStateOf(positionMs) }
    LaunchedEffect(positionMs, isPlaying, reduced) {
        smooth.longValue = positionMs
        if (!isPlaying || reduced) return@LaunchedEffect
        val anchor = withFrameMillis { it }
        while (true) {
            withFrameMillis { frame ->
                val next = positionMs + (frame - anchor)
                smooth.longValue = if (durationMs > 0L) next.coerceAtMost(durationMs) else next
            }
        }
    }
    return smooth
}

// ---- Artwork colour -----------------------------------------------------------

/**
 * A calm accent sampled from the cover at [url], cross-faded whenever the track
 * changes. Keeps the last colour while the next cover loads, and falls back to
 * [fallback] when there is no artwork at all.
 */
@Composable
fun rememberArtworkAccent(url: String?, fallback: Color): State<Color> {
    val context = LocalContext.current
    val reduced = LocalReducedMotion.current
    var target by remember { mutableStateOf(fallback) }
    LaunchedEffect(url) {
        if (url.isNullOrBlank()) {
            target = fallback
            return@LaunchedEffect
        }
        val request = ImageRequest.Builder(context)
            .data(url)
            .size(SAMPLE_SIZE)
            // Software bitmap, so its pixels can be read.
            .allowHardware(false)
            .build()
        val drawable = (context.imageLoader.execute(request) as? SuccessResult)?.drawable ?: return@LaunchedEffect
        target = withContext(Dispatchers.Default) {
            accentFrom(drawable.toBitmap(SAMPLE_SIZE, SAMPLE_SIZE))
        } ?: fallback
    }
    return animateColorAsState(
        targetValue = target,
        animationSpec = tween(durationMillis = if (reduced) 0 else 900),
        label = "artworkAccent",
    )
}

private const val SAMPLE_SIZE = 48

/**
 * Weighted average of the cover's colourful pixels, then toned down so it can
 * sit behind white text on the dark canvas without shouting.
 */
private fun accentFrom(bitmap: Bitmap): Color? {
    val hsv = FloatArray(3)
    var red = 0.0
    var green = 0.0
    var blue = 0.0
    var total = 0.0
    for (y in 0 until bitmap.height) {
        for (x in 0 until bitmap.width) {
            val pixel = bitmap.getPixel(x, y)
            android.graphics.Color.colorToHSV(pixel, hsv)
            // Near-black and near-white pixels say little about the cover's mood.
            if (hsv[2] < 0.15f || (hsv[1] < 0.08f && hsv[2] > 0.9f)) continue
            val weight = (hsv[1] * hsv[2]).toDouble() + 0.05
            red += android.graphics.Color.red(pixel) * weight
            green += android.graphics.Color.green(pixel) * weight
            blue += android.graphics.Color.blue(pixel) * weight
            total += weight
        }
    }
    if (total <= 0.0) return null
    val averaged = android.graphics.Color.rgb((red / total).toInt(), (green / total).toInt(), (blue / total).toInt())
    android.graphics.Color.colorToHSV(averaged, hsv)
    hsv[1] = hsv[1].coerceAtMost(0.6f)
    hsv[2] = hsv[2].coerceIn(0.3f, 0.55f)
    return Color(android.graphics.Color.HSVToColor(hsv))
}
