package com.silverymusic.app.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.silverymusic.app.data.AppContainer
import com.silverymusic.app.theme.CardShape
import com.silverymusic.app.theme.SearchBarShape
import com.silverymusic.app.theme.SilveryBackground
import com.silverymusic.app.theme.SilveryTheme
import com.silverymusic.app.ui.silveryViewModel

@Composable
fun OnboardingCreateAccountScreen(
    onBack: () -> Unit,
    onAccountCreated: () -> Unit,
    onContinueAsGuest: () -> Unit,
    viewModel: OnboardingViewModel = silveryViewModel { OnboardingViewModel(AppContainer.authRepository) },
) {
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    val canContinue = name.isNotBlank() && email.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
    ) {
        Row(modifier = Modifier.padding(top = 24.dp)) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        }

        Text(
            text = "Create your\naccount.",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(top = 16.dp, bottom = 32.dp),
        )

        LabeledField(label = "NAME", value = name, onValueChange = { name = it }, placeholder = "Your name")
        LabeledField(
            label = "EMAIL",
            value = email,
            onValueChange = { email = it },
            placeholder = "email@example.com",
            keyboardType = KeyboardType.Email,
        )
        LabeledField(
            label = "PASSWORD",
            value = password,
            onValueChange = { password = it },
            placeholder = "••••••••",
            keyboardType = KeyboardType.Password,
            helperText = "Never checked, never stored — this screen is a demo.",
            isPassword = true,
        )

        Text(
            text = "Demo build: nothing is sent anywhere and nothing is kept. " +
                "Your name just labels this session until the app closes.",
            style = MaterialTheme.typography.bodySmall,
            color = SilveryTheme.colors.textTertiary,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp),
        )

        Button(
            onClick = {
                viewModel.onSignIn(name, email)
                onAccountCreated()
            },
            enabled = canContinue,
            shape = SearchBarShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.onBackground,
                contentColor = SilveryBackground,
                disabledContainerColor = SilveryTheme.colors.surfaceAlt,
                disabledContentColor = SilveryTheme.colors.textMuted,
            ),
            contentPadding = PaddingValues(vertical = 16.dp),
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            Text(text = "Create Account", style = MaterialTheme.typography.titleMedium)
        }

        Text(
            text = "Or continue as guest",
            style = MaterialTheme.typography.bodyMedium,
            color = SilveryTheme.colors.textTertiary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
                .clickable {
                    viewModel.onContinueAsGuest()
                    onContinueAsGuest()
                },
        )
    }
}

@Composable
private fun LabeledField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    helperText: String? = null,
    isPassword: Boolean = false,
) {
    Column(modifier = modifier.padding(bottom = 20.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = SilveryTheme.colors.textTertiary,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(text = placeholder, color = SilveryTheme.colors.textMuted) },
            singleLine = true,
            shape = CardShape,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = SilveryTheme.colors.border,
                unfocusedBorderColor = Color.Transparent,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
        if (helperText != null) {
            Text(
                text = helperText,
                style = MaterialTheme.typography.bodySmall,
                color = SilveryTheme.colors.textTertiary,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}
