package com.silverymusic.app.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.silverymusic.app.data.AppContainer
import com.silverymusic.app.theme.CardShape
import com.silverymusic.app.theme.SilveryTheme
import com.silverymusic.app.ui.silveryViewModel

@Composable
fun OnboardingSignUpChoiceScreen(
    onCreateAccount: () -> Unit,
    onContinueAsGuest: () -> Unit,
    viewModel: OnboardingViewModel = silveryViewModel { OnboardingViewModel(AppContainer.authRepository) },
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp, vertical = 64.dp),
    ) {
        Text(
            text = "How would you\nlike to start?",
            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 30.sp, lineHeight = 36.sp),
        )
        Text(
            text = "Choose your path — you can always switch later.",
            style = MaterialTheme.typography.bodyLarge,
            color = SilveryTheme.colors.textTertiary,
            modifier = Modifier.padding(top = 12.dp, bottom = 36.dp),
        )

        ChoiceCard(
            title = "Create Account",
            description = "Put a name to this session. Demo build — nothing is sent or saved.",
            onClick = onCreateAccount,
        )
        Spacer(modifier = Modifier.height(16.dp))
        ChoiceCard(
            title = "Continue as Guest",
            description = "Listen straight away. Your queue and settings stay on this device for this session.",
            onClick = {
                viewModel.onContinueAsGuest()
                onContinueAsGuest()
            },
        )
    }
}

@Composable
private fun ChoiceCard(
    title: String,
    description: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            )
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
        }
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = SilveryTheme.colors.textTertiary,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}
