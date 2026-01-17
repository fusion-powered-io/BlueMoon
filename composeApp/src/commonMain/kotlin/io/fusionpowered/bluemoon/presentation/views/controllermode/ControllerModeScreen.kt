package io.fusionpowered.bluemoon.presentation.views.controllermode

import io.fusionpowered.bluemoon.domain.bluetooth.model.PairedDevice
import kotlinx.serialization.Serializable

@Serializable
data class ControllerModeScreen(val device: PairedDevice) {

    sealed interface State {

        data class Connecting(val deviceName: String) : State

    }

}