package com.silverymusic.app.data.local

import android.content.SharedPreferences
import com.silverymusic.app.data.AuthState
import com.silverymusic.app.data.model.Profile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.builtins.ListSerializer

/**
 * Owns the profile list and which one is active, persisted across launches.
 *
 * The main profile is named from the account the user registers; the kid profile
 * ships alongside it as a demo of the feature and is never renamed by sign-in.
 */
class ProfileStore(
    private val prefs: SharedPreferences? = null,
    /** Notified when the active profile changes, so per-profile data can rebind. */
    private val onActiveProfileChanged: (String) -> Unit = {},
) {

    private val _profiles = MutableStateFlow(readProfiles() ?: defaultProfiles())
    val profiles: StateFlow<List<Profile>> = _profiles.asStateFlow()

    private val _activeProfileId = MutableStateFlow(
        prefs?.getString(KEY_ACTIVE, null)?.takeIf { id -> _profiles.value.any { it.id == id } }
            ?: MAIN_PROFILE_ID,
    )
    val activeProfileId: StateFlow<String> = _activeProfileId.asStateFlow()

    fun select(profileId: String) {
        if (_activeProfileId.value == profileId) return
        _activeProfileId.value = profileId
        prefs?.edit()?.putString(KEY_ACTIVE, profileId)?.apply()
        onActiveProfileChanged(profileId)
    }

    fun add(name: String, isKid: Boolean, accentIndex: Int) {
        _profiles.update {
            it + Profile(
                id = "u${nextId()}",
                name = name,
                subtitle = if (isKid) KID_SUBTITLE else "Standard profile",
                isKid = isKid,
                accentIndex = accentIndex,
            )
        }
        persistProfiles()
    }

    fun rename(profileId: String, name: String) {
        _profiles.update { list -> list.map { if (it.id == profileId) it.copy(name = name) else it } }
        persistProfiles()
    }

    fun remove(profileId: String) {
        _profiles.update { list -> list.filterNot { it.id == profileId && it.isRemovable } }
        persistProfiles()
        if (_activeProfileId.value == profileId) {
            _profiles.value.firstOrNull()?.let { select(it.id) }
        }
    }

    /**
     * Reflects the session on the main profile: a registered account uses the name
     * from sign-up, a guest session reads as guest mode, and signing out returns
     * the whole list to its factory state.
     */
    fun applySession(state: AuthState) {
        when (state) {
            is AuthState.SignedIn -> renameMain(
                name = state.displayName.trim().ifBlank { "Listener" },
                subtitle = "Main profile",
            )
            AuthState.Guest -> renameMain(name = "Guest", subtitle = "Guest mode")
            AuthState.SignedOut -> resetToDefaults()
        }
    }

    private fun renameMain(name: String, subtitle: String) {
        _profiles.update { list ->
            list.map {
                if (it.id == MAIN_PROFILE_ID) it.copy(name = name, subtitle = subtitle) else it
            }
        }
        select(MAIN_PROFILE_ID)
        persistProfiles()
    }

    private fun resetToDefaults() {
        _profiles.value = defaultProfiles()
        select(MAIN_PROFILE_ID)
        persistProfiles()
    }

    private fun nextId(): Int {
        val highest = _profiles.value.mapNotNull { it.id.removePrefix("u").toIntOrNull() }.maxOrNull() ?: 0
        return highest + 1
    }

    private fun persistProfiles() {
        val editor = prefs?.edit() ?: return
        editor.putString(KEY_PROFILES, SilveryPrefs.json.encodeToString(SERIALIZER, _profiles.value))
        editor.putString(KEY_ACTIVE, _activeProfileId.value)
        editor.apply()
    }

    private fun readProfiles(): List<Profile>? {
        val raw = prefs?.getString(KEY_PROFILES, null) ?: return null
        // A corrupt or outdated payload falls back to defaults rather than crashing.
        return runCatching { SilveryPrefs.json.decodeFromString(SERIALIZER, raw) }
            .getOrNull()
            ?.takeIf { it.isNotEmpty() }
    }

    companion object {
        const val MAIN_PROFILE_ID = "u1"
        private const val KEY_PROFILES = "profiles"
        private const val KEY_ACTIVE = "active_profile"
        private const val KID_SUBTITLE = "Kid profile · filtered content"
        private val SERIALIZER = ListSerializer(Profile.serializer())

        /** The main profile is renamed at sign-in; Emma stays as the kid-profile demo. */
        fun defaultProfiles() = listOf(
            Profile(MAIN_PROFILE_ID, "Listener", "Main profile", isKid = false, accentIndex = 0, isRemovable = false),
            Profile("u2", "Emma", KID_SUBTITLE, isKid = true, accentIndex = 3),
        )
    }
}
