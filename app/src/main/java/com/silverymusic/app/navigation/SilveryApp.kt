package com.silverymusic.app.navigation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.silverymusic.app.data.AppContainer
import com.silverymusic.app.ui.components.BottomNavBar
import com.silverymusic.app.ui.components.BottomTab
import com.silverymusic.app.ui.components.MiniPlayerBar
import com.silverymusic.app.ui.motion.LocalNavAnimatedVisibilityScope
import com.silverymusic.app.ui.motion.LocalReducedMotion
import com.silverymusic.app.ui.motion.LocalSharedTransitionScope
import com.silverymusic.app.ui.motion.rememberReducedMotion
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
import com.silverymusic.app.ui.screens.seeall.SeeAllScreen
import com.silverymusic.app.ui.screens.seeall.SeeAllSection
import com.silverymusic.app.ui.screens.settings.SettingsScreen
import com.silverymusic.app.ui.screens.sync.SyncSheetScreen
import kotlinx.coroutines.launch

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

/** How long the full player takes to rise over (or sink back into) the app. */
private const val PLAYER_SLIDE_MS = 380

/**
 * Sheets float over whatever screen opened them, so they are overlay state
 * rather than navigation destinations (a destination would replace the screen
 * underneath and leave the sheet over an empty canvas).
 */
private enum class Sheet { DISCOVERY_CONTROL, PROFILE_SWITCHER, SYNC, QUEUE }

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SilveryApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val currentTab = routeToTab(currentRoute)
    val showChrome = currentRoute in chromeRoutes
    val nowPlaying by AppContainer.musicRepository.nowPlaying.collectAsState()
    val reducedMotion = rememberReducedMotion()

    NotificationPermissionOnFirstPlay(isPlaying = nowPlaying.isPlaying)

    var openSheet by rememberSaveable { mutableStateOf<Sheet?>(null) }
    val sheetState = rememberModalBottomSheetState()
    val sheetScope = rememberCoroutineScope()

    /** Slides the sheet away first, then forgets it, so closing is animated too. */
    fun dismissSheet() {
        sheetScope.launch { sheetState.hide() }.invokeOnCompletion { openSheet = null }
    }

    /** Leaves a sheet for a full screen: the sheet goes at once, the screen arrives. */
    fun navigateFromSheet(route: String) {
        openSheet = null
        navController.navigate(route)
    }

    fun openSearch() {
        navController.navigate(Routes.SEARCH) { launchSingleTop = true }
    }

    fun openSeeAll(section: SeeAllSection) {
        navController.navigate(Routes.seeAll(section.name)) { launchSingleTop = true }
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

    CompositionLocalProvider(LocalReducedMotion provides reducedMotion) {
        // One layout hosts both ends of the cover-art morph: the mini player
        // (outside the NavHost) and the full player (inside it).
        SharedTransitionLayout(modifier = Modifier.fillMaxSize()) {
            CompositionLocalProvider(LocalSharedTransitionScope provides this) {
                Column(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
                    Box(modifier = Modifier.weight(1f)) {
                        NavHost(
                            navController = navController,
                            // Onboarding is a first-run step: once the user has signed in or
                            // chosen guest mode, that choice is on device and the app opens
                            // straight to Home. Read once so it can't change mid-session.
                            startDestination = remember {
                                if (AppContainer.authRepository.isOnboarded) Routes.HOME else Routes.ONBOARDING_WELCOME
                            },
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
                                    onOpenProfileSwitcher = { openSheet = Sheet.PROFILE_SWITCHER },
                                    onOpenSeeAll = ::openSeeAll,
                                )
                            }
                            composable(Routes.DISCOVER) {
                                DiscoverScreen(
                                    onOpenSearch = { openSearch() },
                                    onOpenProfileSwitcher = { openSheet = Sheet.PROFILE_SWITCHER },
                                    onOpenDiscoveryControl = { openSheet = Sheet.DISCOVERY_CONTROL },
                                    onOpenSeeAll = ::openSeeAll,
                                )
                            }
                            composable(
                                Routes.SEE_ALL,
                                arguments = listOf(navArgument(Routes.SEE_ALL_ARG) { type = NavType.StringType }),
                            ) { backStackEntry ->
                                val section = SeeAllSection.fromKey(backStackEntry.arguments?.getString(Routes.SEE_ALL_ARG))
                                SeeAllScreen(section = section, onBack = { navController.popBackStack() })
                            }
                            composable(Routes.LIBRARY) {
                                LibraryScreen(
                                    onOpenSearch = { openSearch() },
                                    onOpenProfileSwitcher = { openSheet = Sheet.PROFILE_SWITCHER },
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
                                SearchScreen(onOpenProfileSwitcher = { openSheet = Sheet.PROFILE_SWITCHER })
                            }

                            composable(
                                Routes.PLAYER,
                                // The player rises from the mini bar and sinks back into it,
                                // while the cover art morphs between the two.
                                enterTransition = {
                                    if (reducedMotion) {
                                        EnterTransition.None
                                    } else {
                                        slideInVertically(animationSpec = tween(PLAYER_SLIDE_MS)) { height -> height / 3 } +
                                            fadeIn(animationSpec = tween(PLAYER_SLIDE_MS))
                                    }
                                },
                                popExitTransition = {
                                    if (reducedMotion) {
                                        ExitTransition.None
                                    } else {
                                        slideOutVertically(animationSpec = tween(PLAYER_SLIDE_MS)) { height -> height / 3 } +
                                            fadeOut(animationSpec = tween(PLAYER_SLIDE_MS))
                                    }
                                },
                            ) {
                                CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides this) {
                                    PlayerScreen(
                                        onMinimize = { navController.popBackStack() },
                                        onOpenSync = { openSheet = Sheet.SYNC },
                                        onOpenEq = { navController.navigate(Routes.EQ_PANEL) },
                                        onOpenQueue = { openSheet = Sheet.QUEUE },
                                        onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                                    )
                                }
                            }

                            composable(Routes.SETTINGS) {
                                SettingsScreen(
                                    onBack = { navController.popBackStack() },
                                    onOpenEqualizer = { navController.navigate(Routes.EQ_PANEL) },
                                    onOpenDiscoveryControl = { openSheet = Sheet.DISCOVERY_CONTROL },
                                    onOpenManageProfiles = { navController.navigate(Routes.MANAGE_PROFILES) },
                                    onOpenHowItWorks = { navController.navigate(Routes.HOW_IT_WORKS) },
                                    // Wipe the whole back stack so Back can't return into the
                                    // signed-out app.
                                    onSignedOut = {
                                        navController.navigate(Routes.ONBOARDING_WELCOME) {
                                            popUpTo(navController.graph.id) { inclusive = true }
                                        }
                                    },
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

                    // The chrome tucks away when a full-screen destination opens and
                    // unfolds again on return, instead of popping in and out.
                    AnimatedVisibility(
                        visible = showChrome,
                        enter = if (reducedMotion) EnterTransition.None else expandVertically() + fadeIn(),
                        exit = if (reducedMotion) ExitTransition.None else shrinkVertically() + fadeOut(),
                    ) {
                        CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides this) {
                            Column {
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
                }
            }
        }

        openSheet?.let { sheet ->
            ModalBottomSheet(
                onDismissRequest = { openSheet = null },
                sheetState = sheetState,
            ) {
                // Each opening gets fresh ViewModels, as it did when sheets were
                // destinations; they are cleared when the sheet goes away.
                SheetViewModelScope(sheet) {
                    when (sheet) {
                        Sheet.DISCOVERY_CONTROL -> DiscoveryControlScreen()
                        Sheet.PROFILE_SWITCHER -> ProfileSwitcherScreen(
                            onAddProfile = { navigateFromSheet(Routes.ADD_PROFILE) },
                            onManageProfiles = { navigateFromSheet(Routes.MANAGE_PROFILES) },
                        )
                        Sheet.SYNC -> SyncSheetScreen(onDismiss = ::dismissSheet)
                        Sheet.QUEUE -> QueueSheetScreen()
                    }
                }
            }
        }
    }
}

/** Gives [content] its own ViewModel store for as long as [key] is showing. */
@Composable
private fun SheetViewModelScope(key: Any, content: @Composable () -> Unit) {
    val owner = remember(key) {
        object : ViewModelStoreOwner {
            override val viewModelStore = ViewModelStore()
        }
    }
    DisposableEffect(owner) {
        onDispose { owner.viewModelStore.clear() }
    }
    CompositionLocalProvider(LocalViewModelStoreOwner provides owner, content = content)
}

/**
 * Asks for the notification permission (Android 13+) the first time music
 * actually starts, when the media notification it enables makes sense, rather
 * than on the very first screen. Denial is fine: playback works without it.
 */
@Composable
private fun NotificationPermissionOnFirstPlay(isPlaying: Boolean) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    val context = LocalContext.current
    var asked by rememberSaveable { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* result ignored — non-blocking */ }
    LaunchedEffect(isPlaying) {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (isPlaying && !asked && !granted) {
            asked = true
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
