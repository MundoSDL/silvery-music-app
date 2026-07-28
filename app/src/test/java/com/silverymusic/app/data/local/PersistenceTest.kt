package com.silverymusic.app.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.silverymusic.app.data.AuthState
import com.silverymusic.app.data.DemoAuthRepository
import com.silverymusic.app.data.model.Track
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Covers what the user actually notices: onboarding not reappearing, liked songs
 * surviving a restart, and the registered name landing on the main profile.
 *
 * A "restart" is modelled by building a fresh store over the same
 * SharedPreferences — which is exactly what a new process does.
 */
@RunWith(AndroidJUnit4::class)
class PersistenceTest {

    private lateinit var prefs: SharedPreferences

    private val track = Track(
        id = "t1",
        title = "Glass Ceiling",
        artist = "The Hollow",
        genre = "Indie",
        durationMs = 251_000,
    )

    @Before
    fun setUp() {
        prefs = SilveryPrefs.from(ApplicationProvider.getApplicationContext<Context>())
        prefs.edit().clear().commit()
    }

    // ---- Session / onboarding ---------------------------------------------

    @Test
    fun freshInstallIsNotOnboarded() {
        assertEquals(AuthState.SignedOut, SessionStore(prefs).read())
    }

    @Test
    fun signedInSessionSurvivesRestart() {
        DemoAuthRepository(SessionStore(prefs)).signIn("Ada", "ada@example.com")

        val restarted = DemoAuthRepository(SessionStore(prefs))
        assertTrue("Onboarding must not run again", restarted.isOnboarded)
        assertEquals(AuthState.SignedIn("Ada", "ada@example.com"), restarted.authState.value)
    }

    @Test
    fun guestSessionSurvivesRestart() {
        DemoAuthRepository(SessionStore(prefs)).continueAsGuest()

        assertTrue(DemoAuthRepository(SessionStore(prefs)).isOnboarded)
    }

    @Test
    fun signOutReturnsToOnboarding() {
        val auth = DemoAuthRepository(SessionStore(prefs))
        auth.signIn("Ada", "ada@example.com")

        auth.signOut()

        assertFalse(auth.isOnboarded)
        assertFalse(DemoAuthRepository(SessionStore(prefs)).isOnboarded)
    }

    // ---- Profiles ----------------------------------------------------------

    @Test
    fun registeredNameBecomesMainProfileAndKidProfileRemains() {
        val profiles = ProfileStore(prefs)

        profiles.applySession(AuthState.SignedIn("Ada", "ada@example.com"))

        val main = profiles.profiles.value.first { it.id == ProfileStore.MAIN_PROFILE_ID }
        assertEquals("Ada", main.name)
        assertTrue("Kid profile stays as a demo", profiles.profiles.value.any { it.isKid })
    }

    @Test
    fun guestSessionRenamesMainProfileToGuest() {
        val profiles = ProfileStore(prefs)

        profiles.applySession(AuthState.Guest)

        assertEquals("Guest", profiles.profiles.value.first { it.id == ProfileStore.MAIN_PROFILE_ID }.name)
    }

    @Test
    fun profilesSurviveRestart() {
        ProfileStore(prefs).apply {
            applySession(AuthState.SignedIn("Ada", "ada@example.com"))
            add(name = "Sam", isKid = false, accentIndex = 1)
        }

        val restarted = ProfileStore(prefs)
        assertEquals("Ada", restarted.profiles.value.first { it.id == ProfileStore.MAIN_PROFILE_ID }.name)
        assertTrue(restarted.profiles.value.any { it.name == "Sam" })
    }

    @Test
    fun signOutResetsProfilesToDefaults() {
        val profiles = ProfileStore(prefs)
        profiles.applySession(AuthState.SignedIn("Ada", "ada@example.com"))

        profiles.applySession(AuthState.SignedOut)

        assertEquals("Listener", profiles.profiles.value.first { it.id == ProfileStore.MAIN_PROFILE_ID }.name)
    }

    // ---- Likes -------------------------------------------------------------

    @Test
    fun likedTrackSurvivesRestart() {
        LikesStore(prefs).toggle(track.id) { track }

        val restarted = LikesStore(prefs)
        assertTrue(restarted.isLiked(track.id))
        assertEquals(listOf(track.id), restarted.likedTracks.value.map { it.id })
    }

    @Test
    fun unlikingRemovesTrackAcrossRestart() {
        LikesStore(prefs).apply {
            toggle(track.id) { track }
            toggle(track.id) { track }
        }

        assertFalse(LikesStore(prefs).isLiked(track.id))
    }

    @Test
    fun likesAreKeptPerProfile() {
        val likes = LikesStore(prefs)
        likes.toggle(track.id) { track }

        likes.bindProfile("u2")

        assertFalse("Kid profile must not inherit the main profile's likes", likes.isLiked(track.id))

        likes.bindProfile(ProfileStore.MAIN_PROFILE_ID)
        assertTrue("Switching back restores the original collection", likes.isLiked(track.id))
    }

    @Test
    fun clearAllDropsEveryProfilesLikes() {
        val likes = LikesStore(prefs)
        likes.toggle(track.id) { track }
        likes.bindProfile("u2")
        likes.toggle(track.id) { track }

        likes.clearAll()

        assertFalse(likes.isLiked(track.id))
        assertTrue(LikesStore(prefs).likedTracks.value.isEmpty())
        LikesStore(prefs).let {
            it.bindProfile("u2")
            assertTrue(it.likedTracks.value.isEmpty())
        }
    }
}
