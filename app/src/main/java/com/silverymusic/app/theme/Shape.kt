package com.silverymusic.app.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Radii transcribed from Figma: 12dp content cards, 17dp genre pills, 22dp search bar / hero cards.
val CardShape = RoundedCornerShape(12.dp)
val PillShape = RoundedCornerShape(17.dp)
val SearchBarShape = RoundedCornerShape(22.dp)
val BottomSheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)

val SilveryShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(17.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = BottomSheetShape,
)
