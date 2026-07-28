package com.silverymusic.app.data

import com.silverymusic.app.data.local.SessionStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface AuthState {
    data object SignedOut : AuthState
    data object Guest : AuthState
    data class SignedIn(val displayName: String, val email: String) : AuthState
}

/**
 * Demo-only account handling. There is no backend: the name from sign-up simply
 * labels the session and the main profile. The choice *is* remembered on device
 * so onboarding only runs once — no password is ever stored or transmitted.
 */
interface AuthRepository {
    val authState: StateFlow<AuthState>

    /** True once the user has finished onboarding, either signed in or as a guest. */
    val isOnboarded: Boolean get() = authState.value != AuthState.SignedOut

    fun signIn(name: String, email: String)
    fun continueAsGuest()
    fun signOut()
}

class DemoAuthRepository(
    private val store: SessionStore = SessionStore(null),
    /** Lets the container mirror the session onto the profile list. */
    private val onSessionChanged: (AuthState) -> Unit = {},
) : AuthRepository {

    private val _authState = MutableStateFlow(store.read())
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    override fun signIn(name: String, email: String) {
        // Accepts anything; no validation beyond non-blank, no password is kept.
        update(
            AuthState.SignedIn(
                displayName = name.trim().ifBlank { "Listener" },
                email = email.trim(),
            ),
        )
    }

    override fun continueAsGuest() = update(AuthState.Guest)

    override fun signOut() = update(AuthState.SignedOut)

    private fun update(state: AuthState) {
        _authState.value = state
        store.write(state)
        onSessionChanged(state)
    }
}
