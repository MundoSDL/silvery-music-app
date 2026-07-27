package com.silverymusic.app.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import com.silverymusic.app.data.AuthRepository

/**
 * Demo session only. [AuthRepository] keeps the choice in memory for the length
 * of the app run — there is no account system behind these screens.
 */
class OnboardingViewModel(private val authRepository: AuthRepository) : ViewModel() {

    fun onSignIn(name: String, email: String) = authRepository.signIn(name, email)

    fun onContinueAsGuest() = authRepository.continueAsGuest()
}
