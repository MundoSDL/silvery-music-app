package com.silverymusic.app.ui.screens.help

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.silverymusic.app.theme.CardShape
import com.silverymusic.app.theme.SearchBarShape
import com.silverymusic.app.theme.SilveryBackground
import com.silverymusic.app.theme.SilveryTheme
import com.silverymusic.app.ui.components.SilveryTopBar

/**
 * A short "how the app works" explainer. Shown once during onboarding and reachable
 * again from Settings, so the guidance is never a one-time thing a user can miss.
 *
 * @param ctaLabel label for the bottom action button ("Continue" in onboarding, "Got it" from Settings).
 * @param onCta invoked by the button — advances onboarding, or closes when opened from Settings.
 * @param onBack when non-null, shows a back arrow in a top bar (the Settings entry point).
 */
@Composable
fun HowItWorksScreen(
    ctaLabel: String,
    onCta: () -> Unit,
    onBack: (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        if (onBack != null) {
            SilveryTopBar(title = "How Silvery works", onBack = onBack)
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            if (onBack == null) {
                // Onboarding entry — no top bar, so the title carries the header itself.
                Text(
                    text = "How Silvery works",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                    ),
                    modifier = Modifier.padding(top = 40.dp),
                )
                Text(
                    text = "A quick tour before the music starts.",
                    style = MaterialTheme.typography.titleMedium,
                    color = SilveryTheme.colors.textTertiary,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            HelpPoint(
                icon = Icons.Filled.LibraryMusic,
                title = "Music, and nothing else",
                body = "No feeds, no video, no podcasts. Just your library and the track playing now.",
            )
            HelpPoint(
                icon = Icons.Filled.QueueMusic,
                title = "Your queue is yours",
                body = "Nothing autoplays or reshuffles behind your back. The queue only changes when you change it.",
            )
            HelpPoint(
                icon = Icons.Filled.GraphicEq,
                title = "You steer discovery",
                body = "Discovery Control sets how familiar or adventurous recommendations feel. Adjust it any time.",
            )

            Spacer(modifier = Modifier.height(20.dp))

            // The one thing most people hunt for — spelled out plainly.
            SettingsCallout()

            HelpPoint(
                icon = Icons.Filled.Person,
                title = "Switch profiles",
                body = "Tap your avatar in the top-right of Home to switch profiles or set up a kid profile.",
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        Button(
            onClick = onCta,
            shape = SearchBarShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.onBackground,
                contentColor = SilveryBackground,
            ),
            contentPadding = PaddingValues(vertical = 16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .height(52.dp),
        ) {
            Text(text = ctaLabel, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun HelpPoint(icon: ImageVector, title: String, body: String) {
    Row(modifier = Modifier.padding(vertical = 12.dp)) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp),
            )
        }
        Column(modifier = Modifier.padding(start = 16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = SilveryTheme.colors.textTertiary,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

/** Highlighted card that shows exactly where Settings lives, with the real icon inline. */
@Composable
private fun SettingsCallout() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.surface)
            .padding(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Finding options & settings",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(22.dp),
            )
        }
        Text(
            text = "Open any track to the Now Playing screen and tap the ⋮ in the top-right corner. " +
                "Settings, your equalizer, and profiles all live there.",
            style = MaterialTheme.typography.bodyMedium,
            color = SilveryTheme.colors.textSecondary,
            modifier = Modifier.padding(top = 8.dp),
        )
        Text(
            text = "Tip: swipe down anywhere on the Now Playing screen to slide it back to your music.",
            style = MaterialTheme.typography.bodySmall,
            color = SilveryTheme.colors.textTertiary,
            modifier = Modifier.padding(top = 12.dp),
        )
    }
}
