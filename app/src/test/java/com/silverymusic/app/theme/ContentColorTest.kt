package com.silverymusic.app.theme

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.abs

/**
 * Regression cover for the black-on-black bug: MaterialTheme does not provide
 * LocalContentColor, so it defaults to Color.Black and every Text/Icon that
 * doesn't pass an explicit colour disappears against the dark background.
 */
@RunWith(AndroidJUnit4::class)
class ContentColorTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun themeProvidesNonBlackContentColor() {
        var contentColor: Color? = null
        composeRule.setContent {
            SilveryTheme {
                contentColor = LocalContentColor.current
            }
        }
        composeRule.waitForIdle()

        assertNotEquals("Default content colour must not be black", Color.Black, contentColor)
    }

    @Test
    fun contentColorContrastsWithBackground() {
        var contentColor: Color? = null
        var background: Color? = null
        composeRule.setContent {
            SilveryTheme {
                contentColor = LocalContentColor.current
                background = MaterialTheme.colorScheme.background
            }
        }
        composeRule.waitForIdle()

        val delta = abs(contentColor!!.luminance() - background!!.luminance())
        assertTrue(
            "Content colour ($contentColor) must contrast with background ($background), luminance delta was $delta",
            delta > 0.5f,
        )
    }

    @Test
    fun surfaceRolesAllContrastWithTheirOnColors() {
        val pairs = mutableListOf<Triple<String, Color, Color>>()
        composeRule.setContent {
            SilveryTheme {
                val scheme = MaterialTheme.colorScheme
                pairs += Triple("background/onBackground", scheme.background, scheme.onBackground)
                pairs += Triple("surface/onSurface", scheme.surface, scheme.onSurface)
                pairs += Triple("primary/onPrimary", scheme.primary, scheme.onPrimary)
                pairs += Triple("error/onError", scheme.error, scheme.onError)
                pairs += Triple("inverseSurface/inverseOnSurface", scheme.inverseSurface, scheme.inverseOnSurface)
                pairs += Triple("surfaceVariant/onSurfaceVariant", scheme.surfaceVariant, scheme.onSurfaceVariant)
            }
        }
        composeRule.waitForIdle()

        pairs.forEach { (name, container, onColor) ->
            val delta = abs(container.luminance() - onColor.luminance())
            assertTrue("$name has insufficient contrast (luminance delta $delta)", delta > 0.15f)
        }
    }
}
