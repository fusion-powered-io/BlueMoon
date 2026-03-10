package io.fusionpowered.bluemoon.domain.touchpad.model

data class TouchpadState(
    val leftButton: Boolean = false,
    val rightButton: Boolean = false,
    val middleButton: Boolean = false,
    val deltaX: Int = 0,
    val deltaY: Int = 0,
    val wheel: Int = 0
)