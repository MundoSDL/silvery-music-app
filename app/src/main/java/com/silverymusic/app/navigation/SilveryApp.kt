package com.silverymusic.app.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.silverymusic.app.data.AppContainer
import com.silverymusic.app.ui.components.BottomNavBar
import com.silverymusic.app.ui.components.BottomTab
import com.silverymusic.app.ui.components.MiniPlayerBar
import com.silverymusic.app.ui.screens.discover.DiscoverScreen
import com.silverymusic.app.ui.screens.discoverycontrol.DiscoveryControlScreen
import com.silverymusic.app.ui.screens.equalizer.EqualizerScreen
import com.silverymusic.app.ui.screens.help.HowItWorksScreen
import com.silverymusic.app.ui.screens.home.HomeScreen
import com.silverymusic.app.ui.screens.library.LibraryScreen
import com.silverymusic.app.ui.screens.liked.LikedSongsScreen
import com.silverymusic.app.ui.screens.onboarding.OnboardingConfirmationScreen
import com.silverymusic.app.ui.screens.onboarding.OnboardingCreateAccountScreen
import com.silverymusic.app.ui.screens.onboarding.OnboardingSignUpChoiceScreen
import com.silverymusic.app.ui.screens.onboarding.OnboardingWelcomeScreen
import com.silverymusic.app.ui.screens.player.PlayerScreen
import com.silverymusic.app.ui.screens.profiles.AddProfileScreen
import com.silverymusic.app.ui.screens.profiles.ManageProfilesScreen
import com.silverymusic.app.ui.screens.profileswitcher.ProfileSwitcherScreen
import com.silverymusic.app.ui.screens.queue.QueueSheetScreen
import com.silverymusic.app.ui.screens.search.SearchScreen
import com.silverymusic.app.ui.screens.settings.SettingsScreen
import com.silverymusic.app.ui.screens.sync.SyncSheetScreen

private val BottomTab.route: String
    get() = when (this) {
        BottomTab.HOME -> Routes.HOME
        BottomTab.DISCOVER -> Routes.DISCOVER
        BottomTab.LIBRARY -> Routes.LIBRARY
    }

private fun routeToTab(route: String?): BottomTab? = when (route) {
    Routes.HOME -> BottomTab.HOME
    Routes.DISCOVER -> BottomTab.DISCOVER
    Routes.LIBRARY -> BottomTab.LIBRARY
    else -> null
}

/** The browsing surfaces that carry the mini player + bottom nav. Search is one, though it's no longer a tab. */
private val chromeRoutes = setOf(Routes.HOME, Routes.DISCOVER, Routes.LIBRARY, Routes.SEARCH)

/** Duration of the shared screen cross-fade between destinations, in millis. */
private const val SCREEN_FADE_MS = 240

@Composable
fun SilveryApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val currentTab = routeToTab(currentRoute)
    val showChrome = currentRoute in chromeRoutes
    val nowPlaying by AppContainer.musicRepository.nowPlaying.collectAsState()

    fun openSearch() {
        navController.navigate(Routes.SEARCH) { launchSingleTop = true }
    }

    fun navigateToTab(tab: BottomTab) {
        navController.navigate(tab.route) {
            popUpTo(Routes.HOME) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun navigateIntoApp() {
        navController.navigate(Routes.HOME) {
            popUpTo(Routes.ONBOARDING_WELCOME) { inclusive = true }
        }
    }

    Column(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
        Box(modifier = Modifier.weight(1f)) {
            NavHost(
                navController = navController,
                startDestination = Routes.ONBOARDING_WELCOME,
                // A single quick cross-fade everywhere makes moving between tabs,
                // Search and detail screens read as one continuous surface rather
                // than a stack of separate pages.
                enterTransition = { fadeIn(animationSpec = tween(SCREEN_FADE_MS)) },
                exitTransition = { fadeOut(animationSpec = tween(SCREEN_FADE_MS)) },
                popEnterTransition = { fadeIn(animationSpec = tween(SCREEN_FADE_MS)) },
                popExitTransition = { fadeOut(animationSpec = tween(SCREEN_FADE_MS)) },
            ) {
                composable(Routes.ONBOARDING_WELCOME) {
                    OnboardingWelcomeScreen(
                        onGetStarted = { navController.navigate(Routes.ONBOARDING_SIGNUP_CHOICE) },
                        // Skipping straight to a guest session still lands on the
                        // quick tour, so nobody misses where Settings lives.
                        onSignIn = { navController.navigate(Routes.ONBOARDING_HOW_IT_WORKS) },
                    )
                }
                // The tour is the last thing before the app opens — it pops up the
                // moment onboarding finishes, then drops you into Home.
                composable(Routes.ONBOARDING_HOW_IT_WORKS) {
                    HowItWorksScreen(
                        ctaLabel = "Start Listening",
                        onCta = { navigateIntoApp() },
                    )
                }
                composable(Routes.ONBOARDING_SIGNUP_CHOICE) {
                    OnboardingSignUpChoiceScreen(
                        onCreateAccount = { navController.navigate(Routes.ONBOARDING_CREATE_ACCOUNT) },
                        onContinueAsGuest = { navController.navigate(Routes.ONBOARDING_GUEST_IN) },
                    )
                }
                composable(Routes.ONBOARDING_CREATE_ACCOUNT) {
                    OnboardingCreateAccountScreen(
                        onBack = { navController.popBackStack() },
                        onAccountCreated = { navController.navigate(Routes.ONBOARDING_YOURE_IN) },
                        onContinueAsGuest = { navController.navigate(Routes.ONBOARDING_GUEST_IN) },
                    )
                }
                composable(Routes.ONBOARDING_YOURE_IN) {
                    OnboardingConfirmationScreen(
                        checklist = listOf(
                            "Personalised discovery",
                            "Queue sovereignty — always yours",
                            "Sync Play with friends",
                        ),
                        onStartListening = { navController.navigate(Routes.ONBOARDING_HOW_IT_WORKS) },
                        ctaLabel = "Continue",
                    )
                }
                composable(Routes.ONBOARDING_GUEST_IN) {
                    OnboardingConfirmationScreen(
                        checklist = listOf(
                            "Discover Songs and features",
                            "Try the Discovery Mixer",
                            "Learn how we support singers and artists",
                        ),
                        onStartListening = { navController.navigate(Routes.ONBOARDING_HOW_IT_WORKS) },
                        ctaLabel = "Continue",
                    )
                }

                composable(Routes.HOME) {
                    HomeScreen(
                        onOpenSearch = { openSearch() },
                        onOpenProfileSwitcher = { navController.navigate(Routes.PROFILE_SWITCHER) },
                    )
                }
                composable(Routes.DISCOVER) {
                    DiscoverScreen(
                        onOpenSearch = { openSearch() },
                        onOpenProfileSwitcher = { navController.navigate(Routes.PROFILE_SWITCHER) },
                        onOpenDiscoveryControl = { navController.navigate(Routes.DISCOVERY_CONTROL) },
                    )
                }
                composable(Routes.LIBRARY) {
                    LibraryScreen(
                        onOpenSearch = { openSearch() },
                        onOpenProfileSwitcher = { navController.navigate(Routes.PROFILE_SWITCHER) },
                        onOpenLikedSongs = { navController.navigate(Routes.LIKED_SONGS) },
                    )
                }
                composable(Routes.LIKED_SONGS) {
                    LikedSongsScreen(onBack = { navController.popBackStack() })
                }
                composable(
                    Routes.SEARCH,
                    // Search rises gently from the search bar and settles back down,
                    // so it feels like the same surface expanding, not a new page.
                    enterTransition = {
                        fadeIn(animationSpec = tween(SCREEN_FADE_MS)) +
                            slideInVertically(animationSpec = tween(SCREEN_FADE_MS)) { height -> height / 12 }
                    },
                    popExitTransition = {
                        fadeOut(animationSpec = tween(SCREEN_FADE_MS)) +
                            slideOutVertically(animationSpec = tween(SCREEN_FADE_MS)) { height -> height / 12 }
                    },
                ) {
                    SearchScreen(onOpenProfileSwitcher = { navController.navigate(Routes.PROFILE_SWITCHER) })
                }

                composable(Routes.PLAYER) {
                    PlayerScreen(
                        onMinimize = { navController.popBackStack() },
                        onOpenSync = { navController.navigate(Routes.SYNC_SHEET) },
                        onOpenEq = { navController.navigate(Routes.EQ_PANEL) },
                        onOpenQueue = { navController.navigate(Routes.QUEUE_SHEET) },
                        onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                    )
                }

                composable(Routes.DISCOVERY_CONTROL) {
                    BottomSheetRoute(onDismiss = { navController.popBackStack() }) {
                        DiscoveryControlScreen()
                    }
                }
                composable(Routes.PROFILE_SWITCHER) {
                    BottomSheetRoute(onDismiss = { navController.popBackStack() }) {
                        ProfileSwitcherScreen(
                            onAddProfile = { navController.navigate(Routes.ADD_PROFILE) },
                            onManageProfiles = { navController.navigate(Routes.MANAGE_PROFILES) },
                        )
                    }
                }
                composable(Routes.SYNC_SHEET) {
                    BottomSheetRoute(onDismiss = { navController.popBackStack() }) { dismiss ->
                        SyncSheetScreen(onDismiss = dismiss)
                    }
                }
                composable(Routes.QUEUE_SHEET) {
                    BottomSheetRoute(onDismiss = { navController.popBackStack() }) {
                        QueueSheetScreen()
                    }
                }

                composable(Routes.SETTINGS) {
                    SettingsScreen(
                        onBack = { navController.popBackStack() },
                        onOpenEqualizer = { navController.navigate(Routes.EQ_PANEL) },
                        onOpenDiscoveryControl = { navController.navigate(Routes.DISCOVERY_CONTROL) },
                        onOpenManageProfiles = { navController.navigate(Routes.MANAGE_PROFILES) },
                        onOpenHowItWorks = { navController.navigate(Routes.HOW_IT_WORKS) },
                    )
                }
                composable(Routes.HOW_IT_WORKS) {
                    HowItWorksScreen(
                        ctaLabel = "Got it",
                        onCta = { navController.popBackStack() },
                        onBack = { navController.popBackStack() },
                    )
                }
                composable(Routes.EQ_PANEL) {
                    EqualizerScreen(onBack = { navController.popBackStack() })
                }
                composable(Routes.MANAGE_PROFILES) {
                    ManageProfilesScreen(
                        onBack = { navController.popBackStack() },
                        onAddProfile = { navController.navigate(Routes.ADD_PROFILE) },
                    )
                }
                composable(Routes.ADD_PROFILE) {
                    AddProfileScreen(
                        onBack = { navController.popBackStack() },
                        onCreated = { navController.popBackStack() },
                    )
                }
            }
        }

        if (showChrome) {
            MiniPlayerBar(
                nowPlaying = nowPlaying,
                onOpenPlayer = { navController.navigate(Routes.PLAYER) },
                onTogglePlayPause = { AppContainer.musicRepository.togglePlayPause() },
                onSkipNext = { AppContainer.musicRepository.skipNext() },
                onToggleLike = { AppContainer.musicRepository.toggleLike(nowPlaying.track.id) },
            )
            BottomNavBar(
                selectedTab = currentTab,
                onTabSelected = ::navigateToTab,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BottomSheetRoute(
    onDismiss: () -> Unit,
    content: @Composable (dismiss: () -> Unit) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
    ) {
        content(onDismiss)
    }
}
