package com.silverymusic.app.data.model

import kotlinx.serialization.Serializable

/** Serializable so profiles survive a restart (see `data.local.ProfileStore`). */
@Serializable
data class Profile(
    val id: String,
    val name: String,
    val subtitle: String,
    val isKid: Boolean = false,
    /** Index into `ProfileAccents`; keeps the model free of Compose types. */
    val accentIndex: Int = 0,
    /** The main profile can't be removed — every account keeps one. */
    val isRemovable: Boolean = true,
)
