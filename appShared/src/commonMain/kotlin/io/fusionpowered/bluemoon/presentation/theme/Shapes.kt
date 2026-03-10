package io.fusionpowered.bluemoon.presentation.theme


import androidx.compose.ui.geometry.Rect
import androidx.graphics.shapes.RoundedPolygon

private fun RoundedPolygon.getBounds() = calculateBounds().let {
    Rect(
        it[0], it[1],
        it[2], it[3]
    )
}