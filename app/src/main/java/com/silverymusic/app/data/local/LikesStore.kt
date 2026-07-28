package com.silverymusic.app.data.local

import android.content.SharedPreferences
import com.silverymusic.app.data.model.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.builtins.ListSerializer

/**
 * Liked songs, kept per profile and persisted on device.
 *
 * Each profile gets its own key, so switching to the kid profile shows that
 * profile's collection rather than the main account's. The in-memory map keeps
 * insertion order so the newest like can be shown first.
 */
class LikesStore(private val prefs: SharedPreferences? = null) {

    private var profileId: String = ProfileStore.MAIN_PROFILE_ID
    private var likedById = LinkedHashMap<String, Track>()

    private val _likedTracks = MutableStateFlow<List<Track>>(emptyList())
    val likedTracks: StateFlow<List<Track>> = _likedTracks.asStateFlow()

    init {
        load()
    }

    /** Points the store at [id]'s collection; called when the active profile changes. */
    fun bindProfile(id: String) {
        if (id == profileId) return
        profileId = id
        load()
    }

    fun isLiked(trackId: String): Boolean = trackId in likedById

    /**
     * Flips the like for [trackId] and returns the new state. [resolve] supplies the
     * full track when liking, so the Liked Songs list can render without a re-fetch.
     */
    fun toggle(trackId: String, resolve: () -> Track?): Boolean {
        val liked = trackId !in likedById
        if (liked) {
            val track = resolve() ?: return false
            likedById[trackId] = track.copy(isLiked = true)
        } else {
            likedById.remove(trackId)
        }
        publish()
        return liked
    }

    /** Drops every profile's collection — used when signing out. */
    fun clearAll() {
        likedById = LinkedHashMap()
        _likedTracks.value = emptyList()
        val editor = prefs?.edit() ?: return
        prefs.all.keys.filter { it.startsWith(KEY_PREFIX) }.forEach(editor::remove)
        editor.apply()
    }

    private fun publish() {
        _likedTracks.value = likedById.values.reversed()
        prefs?.edit()
            ?.putString(key(), SilveryPrefs.json.encodeToString(SERIALIZER, likedById.values.toList()))
            ?.apply()
    }

    private fun load() {
        val raw = prefs?.getString(key(), null)
        val stored = raw
            ?.let { runCatching { SilveryPrefs.json.decodeFromString(SERIALIZER, it) }.getOrNull() }
            .orEmpty()
        likedById = LinkedHashMap<String, Track>().apply {
            stored.forEach { put(it.id, it.copy(isLiked = true)) }
        }
        _likedTracks.value = likedById.values.reversed()
    }

    private fun key() = "$KEY_PREFIX$profileId"

    private companion object {
        const val KEY_PREFIX = "likes_"
        val SERIALIZER = ListSerializer(Track.serializer())
    }
}
