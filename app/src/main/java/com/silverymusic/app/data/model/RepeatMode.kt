package com.silverymusic.app.data.model

/**
 * The three repeat states the player cycles through on each tap of the repeat
 * button: no repeat, loop the whole queue, loop the current track.
 */
enum class RepeatMode {
    OFF,
    ALL,
    ONE,
    ;

    /** Next state in the Off → All → One → Off cycle. */
    fun next(): RepeatMode = when (this) {
        OFF -> ALL
        ALL -> ONE
        ONE -> OFF
    }
}
