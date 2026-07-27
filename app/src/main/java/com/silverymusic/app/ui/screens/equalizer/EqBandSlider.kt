package com.silverymusic.app.ui.screens.equalizer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.silverymusic.app.data.model.EqSettings
import com.silverymusic.app.theme.SilveryTheme
import kotlin.math.roundToInt

private val TrackWidth = 4.dp
private val ThumbRadius = 9.dp

/**
 * One vertical EQ band. Drawn rather than built from [androidx.compose.material3.Slider]
 * because the control is vertical, is centred on 0 dB, and fills from the centre
 * outward — none of which the stock horizontal slider expresses.
 */
@Composable
fun EqBandSlider(
    label: String,
    gainDb: Float,
    enabled: Boolean,
    onGainChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    trackHeight: Dp = 180.dp,
) {
    val currentOnGainChange by rememberUpdatedState(onGainChange)
    val accent = MaterialTheme.colorScheme.onBackground
    val trackColor = SilveryTheme.colors.surfaceAlt
    val disabledColor = SilveryTheme.colors.textMuted

    val range = EqSettings.MAX_GAIN_DB - EqSettings.MIN_GAIN_DB

    Column(
        modifier = modifier.width(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = formatGain(gainDb),
            style = MaterialTheme.typography.labelSmall,
            color = if (enabled && gainDb != 0f) accent else SilveryTheme.colors.textMuted,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(trackHeight)
                .semantics { contentDescription = "$label hertz band, ${formatGain(gainDb)} decibels" }
                .pointerInput(enabled) {
                    if (!enabled) return@pointerInput
                    val inset = ThumbRadius.toPx()
                    // Tap anywhere on the track to jump the band there.
                    detectTapGestures { offset ->
                        currentOnGainChange(gainForY(offset.y, size.height.toFloat(), inset, range))
                    }
                }
                .pointerInput(enabled) {
                    if (!enabled) return@pointerInput
                    val inset = ThumbRadius.toPx()
                    detectDragGestures { change, _ ->
                        currentOnGainChange(gainForY(change.position.y, size.height.toFloat(), inset, range))
                    }
                },
        ) {
            val trackPx = TrackWidth.toPx()
            val thumbPx = ThumbRadius.toPx()
            val centerX = size.width / 2f
            // Inset by the thumb radius so the thumb never clips at the extremes.
            val top = thumbPx
            val usableHeight = size.height - thumbPx * 2
            val centerY = top + usableHeight / 2f
            val fraction = (gainDb - EqSettings.MIN_GAIN_DB) / range
            val thumbY = top + usableHeight * (1f - fraction)

            val activeColor = if (enabled) accent else disabledColor

            drawRoundRect(
                color = trackColor,
                topLeft = Offset(centerX - trackPx / 2f, top),
                size = Size(trackPx, usableHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(trackPx / 2f),
            )

            // Fill from the 0 dB centre line toward the thumb, so cut and boost read differently.
            val fillTop = minOf(centerY, thumbY)
            val fillHeight = kotlin.math.abs(thumbY - centerY)
            if (fillHeight > 0f) {
                drawRoundRect(
                    color = activeColor,
                    topLeft = Offset(centerX - trackPx / 2f, fillTop),
                    size = Size(trackPx, fillHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(trackPx / 2f),
                )
            }

            drawLine(
                color = trackColor.copy(alpha = 0.9f),
                start = Offset(centerX - trackPx * 2f, centerY),
                end = Offset(centerX + trackPx * 2f, centerY),
                strokeWidth = 1.dp.toPx(),
            )

            drawCircle(color = activeColor, radius = thumbPx, center = Offset(centerX, thumbY))
            drawCircle(
                color = Color.Black.copy(alpha = 0.35f),
                radius = thumbPx,
                center = Offset(centerX, thumbY),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx()),
            )
        }

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = SilveryTheme.colors.textTertiary,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

/** [inset] matches the thumb-radius padding the track is drawn with, so a drag lands where it looks. */
private fun gainForY(y: Float, heightPx: Float, inset: Float, range: Float): Float {
    val usable = (heightPx - inset * 2).coerceAtLeast(1f)
    val fraction = (1f - ((y - inset) / usable)).coerceIn(0f, 1f)
    val raw = EqSettings.MIN_GAIN_DB + fraction * range
    // Snap to whole decibels — matches the numeric readout above each band.
    return raw.roundToInt().toFloat()
}

private fun formatGain(gainDb: Float): String {
    val rounded = gainDb.roundToInt()
    return when {
        rounded > 0 -> "+$rounded"
        else -> "$rounded"
    }
}
