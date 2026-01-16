package io.fusionpowered.bluemoon.presentation.controllermode

import kotlinx.serialization.Serializable

@Serializable
data class ControllerModeRoute(
    val connection: String
)