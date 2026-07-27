package com.silverymusic.app.data.model

enum class DiscoveryMode(val label: String, val description: String) {
    FAMILIAR(
        label = "Familiar",
        description = "Mostly artists and genres you already love. Minimal surprises.",
    ),
    BALANCED(
        label = "Balanced",
        description = "A mix of favorites and new discovery, tuned to your taste.",
    ),
    ADVENTUROUS(
        label = "Adventurous",
        description = "Breaks out of your usual genres. Expect the unexpected.",
    ),
}
