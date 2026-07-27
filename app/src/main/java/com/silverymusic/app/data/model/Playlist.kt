package com.silverymusic.app.data.model

data class Playlist(
    val id: String,
    val title: String,
    val subtitle: String,
    val trackCount: Int,
    val artworkUrl: String? = null,
    val durationLabel: String? = null,
)
