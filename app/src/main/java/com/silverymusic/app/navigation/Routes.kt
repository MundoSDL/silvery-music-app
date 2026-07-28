package com.silverymusic.app.navigation

object Routes {
    const val ONBOARDING_WELCOME = "onboarding/welcome"
    const val ONBOARDING_HOW_IT_WORKS = "onboarding/how_it_works"
    const val ONBOARDING_SIGNUP_CHOICE = "onboarding/signup_choice"
    const val ONBOARDING_CREATE_ACCOUNT = "onboarding/create_account"
    const val ONBOARDING_YOURE_IN = "onboarding/youre_in"
    const val ONBOARDING_GUEST_IN = "onboarding/guest_in"

    const val HOME = "home"
    const val DISCOVER = "discover"
    const val LIBRARY = "library"
    const val LIKED_SONGS = "liked_songs"
    const val SEARCH = "search"

    const val PLAYER = "player"
    const val DISCOVERY_CONTROL = "discovery_control"
    const val PROFILE_SWITCHER = "profile_switcher"
    const val SYNC_SHEET = "sync_sheet"
    const val QUEUE_SHEET = "queue_sheet"

    /** Full list for a home/discover section. [SEE_ALL_ARG] carries the section key. */
    const val SEE_ALL_ARG = "section"
    const val SEE_ALL = "see_all/{$SEE_ALL_ARG}"
    fun seeAll(section: String) = "see_all/$section"

    const val SETTINGS = "settings"
    const val HOW_IT_WORKS = "how_it_works"
    const val EQ_PANEL = "eq_panel"
    const val MANAGE_PROFILES = "manage_profiles"
    const val ADD_PROFILE = "add_profile"

    val mainTabs = listOf(HOME, DISCOVER, LIBRARY, SEARCH)
}
