package com.silverymusic.app.data.model

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
