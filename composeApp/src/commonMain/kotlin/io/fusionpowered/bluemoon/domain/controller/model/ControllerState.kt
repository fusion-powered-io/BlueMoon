package io.fusionpowered.bluemoon.domain.controller.model

data class ControllerState(
    val a: Boolean = false,
    val b: Boolean = false,
    val x: Boolean = false,
    val y: Boolean = false,

    // D-Pad (Hat Switch)
    val dpadX: Float = 0f, // Input Expected: -1f (Left), 0f (Released) and 1f (Right)
    val dpadY: Float = 0f, // Input Expected: -1f (Up), 0f (Released) and 1f (Down)

    val l1: Boolean = false,
    val r1: Boolean = false,
    // Input Expected: 0f (Released) to 1f (Pressed)
    val l2: Float = 0f,
    val r2: Float = 0f,

    // Input Expected: -1f (Left/Up) to 1f (Right/Down)
    val leftStickX: Float = 0f,
    val leftStickY: Float = 0f,
    val rightStickX: Float = 0f,
    val rightStickY: Float = 0f,
    val l3: Boolean = false,
    val r3: Boolean = false,

    val start: Boolean = false,     // Menu
    val select: Boolean = false,    // View, Back
    val guide: Boolean = false,     // Logo button
)