package com.silverymusic.app.data.model

enum class AudioQuality(val label: String, val detail: String) {
    STANDARD("Standard", "96 kbps · uses least data"),
    HIGH("High", "256 kbps · balanced"),
    LOSSLESS("Lossless", "FLAC · best fidelity, largest files"),
}

/**
 * Defaults encode the product principles rather than platform convention:
 * autoplay and notifications start off (Queue Sovereignty, Sanctuary by Default).
 */
data class AppSettings(
    val audioQuality: AudioQuality = AudioQuality.HIGH,
    val gaplessPlayback: Boolean = true,
    val volumeNormalization: Boolean = false,
    val autoplaySimilar: Boolean = false,
    val notifications: Boolean = false,
    val privateSession: Boolean = false,
)
