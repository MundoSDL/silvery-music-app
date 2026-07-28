package com.silverymusic.app.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.serialization.json.Json

/**
 * On-device persistence for the demo: a single SharedPreferences file holding the
 * session, the profile list and each profile's liked songs.
 *
 * SharedPreferences rather than a database on purpose — the whole payload is a
 * handful of small JSON blobs, and this keeps the build dependency-free. Every
 * store accepts a null [SharedPreferences] so previews, tests and the offline
 * fake run entirely in memory.
 */
object SilveryPrefs {
    private const val FILE = "silvery.prefs"

    fun from(context: Context): SharedPreferences =
        context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    /** Lenient so a field added later can't make an old payload unreadable. */
    val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
}
