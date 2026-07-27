package com.silverymusic.app.data.model

enum class EqPreset(val label: String) {
    FLAT("Flat"),
    BASS_BOOST("Bass Boost"),
    VOCAL("Vocal"),
    ACOUSTIC("Acoustic"),
    LATE_NIGHT("Late Night"),
    CUSTOM("Custom"),
}

/**
 * Seven-band graphic EQ. [gains] holds one value per band in the range
 * [MIN_GAIN_DB]..[MAX_GAIN_DB], index-aligned with [BAND_LABELS].
 */
data class EqSettings(
    val enabled: Boolean = true,
    val preset: EqPreset = EqPreset.FLAT,
    val gains: List<Float> = List(BAND_COUNT) { 0f },
) {
    companion object {
        const val BAND_COUNT = 7
        const val MIN_GAIN_DB = -12f
        const val MAX_GAIN_DB = 12f

        val BAND_LABELS = listOf("60", "150", "400", "1k", "2.4k", "6k", "15k")

        fun gainsFor(preset: EqPreset): List<Float> = when (preset) {
            EqPreset.FLAT -> listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f)
            EqPreset.BASS_BOOST -> listOf(8f, 6f, 3f, 0f, -1f, -1f, 0f)
            EqPreset.VOCAL -> listOf(-3f, -1f, 2f, 5f, 5f, 2f, -1f)
            EqPreset.ACOUSTIC -> listOf(3f, 2f, 0f, 1f, 3f, 4f, 3f)
            EqPreset.LATE_NIGHT -> listOf(-2f, 0f, 2f, 3f, 1f, -1f, -3f)
            EqPreset.CUSTOM -> List(BAND_COUNT) { 0f }
        }
    }
}
