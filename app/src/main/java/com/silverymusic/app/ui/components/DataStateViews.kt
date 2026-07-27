package com.silverymusic.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.silverymusic.app.data.DataError
import com.silverymusic.app.theme.SilveryTheme

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
) {
    if (!isLoading && error == null && !isEmpty) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
    ) {
        when {
            isLoading -> Text(
                text = loadingMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = SilveryTheme.colors.textTertiary,
            )

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
