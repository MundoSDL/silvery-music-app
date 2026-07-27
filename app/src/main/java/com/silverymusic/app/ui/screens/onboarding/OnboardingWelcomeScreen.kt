package com.silverymusic.app.ui.screens.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.silverymusic.app.data.AppContainer
import com.silverymusic.app.theme.SearchBarShape
import com.silverymusic.app.theme.SilveryAccentDim
import com.silverymusic.app.theme.SilveryBackground
import com.silverymusic.app.theme.SilveryTheme
import com.silverymusic.app.ui.silveryViewModel

@Composable
fun OnboardingWelcomeScreen(
    onGetStarted: () -> Unit,
    onSignIn: () -> Unit,
    viewModel: OnboardingViewModel = silveryViewModel { OnboardingViewModel(AppContainer.authRepository) },
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            GradientOrb(modifier = Modifier.size(280.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "SILVERY",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 34.sp,
                        letterSpacing = 4.sp,
                    ),
                )
                Text(
                    text = "Music. Just music.",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 12.dp),
                )
                Text(
                    text = "The listening experience you deserve.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SilveryTheme.colors.textTertiary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }

        Button(
            onClick = onGetStarted,
            shape = SearchBarShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.onBackground,
                contentColor = SilveryBackground,
            ),
            contentPadding = PaddingValues(vertical = 16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(52.dp),
        ) {
            Text(text = "Get Started", style = MaterialTheme.typography.titleMedium)
        }

        // No accounts exist in this build, so the shortcut is honest about it:
        // it drops straight into a guest session.
        Text(
            text = "Skip and listen as a guest",
            style = MaterialTheme.typography.bodyMedium,
            color = SilveryTheme.colors.textTertiary,
            modifier = Modifier
                .padding(top = 20.dp)
                .clickable {
                    viewModel.onContinueAsGuest()
                    onSignIn()
                },
        )
    }
}

@Composable
private fun GradientOrb(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val brush = Brush.radialGradient(
            colors = listOf(SilveryAccentDim.copy(alpha = 0.35f), SilveryBackground.copy(alpha = 0f)),
            center = Offset(size.width / 2, size.height / 2),
            radius = size.minDimension / 2,
        )
        drawCircle(brush = brush, radius = size.minDimension / 2)
    }
}
