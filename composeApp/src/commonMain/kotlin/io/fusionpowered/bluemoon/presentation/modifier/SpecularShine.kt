package io.fusionpowered.bluemoon.presentation.modifier

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color


fun Modifier.specularShine(progress: Float = 0f) =
    this
        .drawBehind {
            val shineX = progress * size.width
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    start = Offset(shineX - 200f, 0f),
                    end = Offset(shineX - 40f, size.height)
                )
            )
        }
