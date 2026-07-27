package com.silverymusic.app.ui.screens.sync

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.silverymusic.app.data.AppContainer
import com.silverymusic.app.data.model.ListeningStatus
import com.silverymusic.app.theme.SearchBarShape
import com.silverymusic.app.theme.SilveryBackground
import com.silverymusic.app.theme.SilveryTheme
import com.silverymusic.app.ui.silveryViewModel

@Composable
fun SyncSheetScreen(
    onDismiss: () -> Unit,
    viewModel: SyncViewModel = silveryViewModel { SyncViewModel(AppContainer.musicRepository) },
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
        Text(text = "Sync with Friends", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 4.dp))

        val status = uiState.listeningStatus
        if (status is ListeningStatus.Synced) {
            Text(
                text = "Synced with ${status.friendName}",
                style = MaterialTheme.typography.bodyLarge,
                color = SilveryTheme.colors.textTertiary,
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp),
            )
            OutlinedButton(
                onClick = { viewModel.onEndSync(); onDismiss() },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = "End session")
            }
        } else {
            Text(
                text = "Search a friend or recent listener to listen together in real time.",
                style = MaterialTheme.typography.bodyMedium,
                color = SilveryTheme.colors.textTertiary,
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp),
            )
            uiState.recentListeners.forEach { name ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.onStartSync(name); onDismiss() }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(SilveryTheme.colors.artPlaceholder),
                    )
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.padding(start = 12.dp),
                    )
                }
            }
            Button(
                onClick = {},
                shape = SearchBarShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onBackground,
                    contentColor = SilveryBackground,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 8.dp),
            ) {
                Text(text = "Generate session link")
            }
        }
    }
}
