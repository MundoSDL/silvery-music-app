package com.silverymusic.app.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Tokens outside M3's three theme slots (colorScheme/typography/shapes) — text
 * hierarchy and surfaces specific to this design that don't map onto a single
 * M3 color role.
 */
data class SilveryColors(
    val textSecondary: Color = SilveryTextSecondary,
    val textTertiary: Color = SilveryTextTertiary,
    val textMuted: Color = SilveryTextMuted,
    val iconInactive: Color = SilveryIconInactive,
    val navInactive: Color = SilveryNavInactive,
    val surfaceAlt: Color = SilverySurfaceAlt,
    val miniPlayerSurface: Color = SilveryMiniPlayerSurface,
    val artPlaceholder: Color = SilveryArtPlaceholder,
    val border: Color = SilveryBorder,
    val accentDim: Color = SilveryAccentDim,
    val liveDot: Color = SilveryLiveDot,
    val liked: Color = SilveryLiked,
)

val LocalSilveryColors = staticCompositionLocalOf { SilveryColors() }

private val SilveryDarkColorScheme = darkColorScheme(
    primary = SilveryAccent,
    onPrimary = SilveryBackground,
    secondary = SilveryAccentDim,
    onSecondary = SilveryBackground,
    background = SilveryBackground,
    onBackground = SilveryTextPrimary,
    surface = SilverySurface,
    onSurface = SilveryTextPrimary,
    surfaceVariant = SilverySurfaceAlt,
    onSurfaceVariant = SilveryTextSecondary,
    outline = SilveryBorder,
    error = SilveryLiked,
    onError = SilveryBackground,
    // Snackbars render on `inverseSurface` with `inverseOnSurface` text; left
    // unset they fall back to M3's palette and stop matching the app.
    inverseSurface = SilveryAccent,
    inverseOnSurface = SilveryBackground,
    inversePrimary = SilveryBackground,
    // Bottom sheets and menus read these rather than `surface`; without them M3
    // falls back to its own dark greys, which don't match the Figma palette.
    surfaceContainer = SilverySurfaceAlt,
    surfaceContainerLow = SilverySurfaceAlt,
    surfaceContainerLowest = SilveryBackground,
    surfaceContainerHigh = SilverySurface,
    surfaceContainerHighest = SilverySurface,
)

@Composable
fun SilveryTheme(
    // The design has no light theme; system dark-theme state is read only so the
    // theme composable follows the usual shape and stays a drop-in ready for one later.
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalSilveryColors provides SilveryColors()) {
        MaterialTheme(
            colorScheme = SilveryDarkColorScheme,
            typography = SilveryTypography,
            shapes = SilveryShapes,
        ) {
            // MaterialTheme does not set LocalContentColor — it defaults to
            // Color.Black, so any Text/Icon that doesn't pass an explicit colour
            // renders black on this dark background. Provide it here.
            CompositionLocalProvider(
                LocalContentColor provides SilveryDarkColorScheme.onBackground,
                content = content,
            )
        }
    }
}

object SilveryTheme {
    val colors: SilveryColors
        @Composable get() = LocalSilveryColors.current
}
