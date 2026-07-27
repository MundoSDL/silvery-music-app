package com.silverymusic.app.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface AuthState {
    data object SignedOut : AuthState
    data object Guest : AuthState
    data class SignedIn(val displayName: String, val email: String) : AuthState
}

/**
 * Demo-only. There is no backend account system and nothing is persisted — the
 * session lives in memory for the length of the app run. Deliberate: the brief
 * scopes accounts to a demo, so no credentials are stored or transmitted.
 */
interface AuthRepository {
    val authState: StateFlow<AuthState>
    fun signIn(name: String, email: String)
    fun continueAsGuest()
    fun signOut()
}

class DemoAuthRepository : AuthRepository {
    private val _authState = MutableStateFlow<AuthState>(AuthState.SignedOut)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    override fun signIn(name: String, email: String) {
        // Accepts anything; no validation beyond non-blank, no password is kept.
        _authState.value = AuthState.SignedIn(
            displayName = name.trim().ifBlank { "Listener" },
            email = email.trim(),
        )
    }

    override fun continueAsGuest() {
        _authState.value = AuthState.Guest
    }

    override fun signOut() {
        _authState.value = AuthState.SignedOut
    }
}
