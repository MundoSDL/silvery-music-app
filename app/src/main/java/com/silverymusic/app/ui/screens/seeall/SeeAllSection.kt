package com.silverymusic.app.ui.screens.seeall

/**
 * The home/discover sections that a "See all" header can expand into a full
 * list. The [title] doubles as the screen's top-bar title so it matches the
 * header the user tapped.
 */
enum class SeeAllSection(val title: String) {
    RECENTLY_PLAYED("Recently Played"),
    MADE_FOR_YOU("Made For You"),
    TOP_GENRES("Your Top Genres"),
    BROWSE_GENRES("Browse Genres"),
    YOUR_ARTISTS("Your Artists"),
    ;

    companion object {
        /** Safe parse from a nav argument — unknown keys fall back to a sensible default. */
        fun fromKey(key: String?): SeeAllSection =
            entries.firstOrNull { it.name == key } ?: RECENTLY_PLAYED
    }
}
