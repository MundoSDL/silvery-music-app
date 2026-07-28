package com.silverymusic.app.data.local

import android.content.SharedPreferences
import com.silverymusic.app.data.AuthState

/**
 * Remembers who is signed in across launches, which is what lets the app skip
 * onboarding on every run after the first.
 */
class SessionStore(private val prefs: SharedPreferences?) {

    fun read(): AuthState = when (prefs?.getString(KEY_KIND, null)) {
        KIND_GUEST -> AuthState.Guest
        KIND_SIGNED_IN -> AuthState.SignedIn(
            displayName = prefs.getString(KEY_NAME, null).orEmpty().ifBlank { DEFAULT_NAME },
            email = prefs.getString(KEY_EMAIL, null).orEmpty(),
        )
        // Anything else (absent, or an old/unknown value) means "not onboarded".
        else -> AuthState.SignedOut
    }

    fun write(state: AuthState) {
        val editor = prefs?.edit() ?: return
        when (state) {
            is AuthState.SignedIn -> editor
                .putString(KEY_KIND, KIND_SIGNED_IN)
                .putString(KEY_NAME, state.displayName)
                .putString(KEY_EMAIL, state.email)
            AuthState.Guest -> editor
                .putString(KEY_KIND, KIND_GUEST)
                .remove(KEY_NAME)
                .remove(KEY_EMAIL)
            AuthState.SignedOut -> editor
                .remove(KEY_KIND)
                .remove(KEY_NAME)
                .remove(KEY_EMAIL)
        }
        editor.apply()
    }

    private companion object {
        const val KEY_KIND = "auth_kind"
        const val KEY_NAME = "auth_name"
        const val KEY_EMAIL = "auth_email"
        const val KIND_GUEST = "guest"
        const val KIND_SIGNED_IN = "signed_in"
        const val DEFAULT_NAME = "Listener"
    }
}
