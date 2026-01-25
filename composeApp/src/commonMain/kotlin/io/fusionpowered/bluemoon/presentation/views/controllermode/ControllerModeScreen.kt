package io.fusionpowered.bluemoon.presentation.views.controllermode

import io.fusionpowered.bluemoon.domain.bluetooth.model.BluetoothDevice
import io.fusionpowered.bluemoon.domain.controller.model.ControllerState
import kotlinx.serialization.Serializable

@Serializable
data class ControllerModeScreen(val device: BluetoothDevice) {

    sealed interface State {

        data class Disconnected(
            val onDisconnected: () -> Unit
        ) : State

        data class Connecting(
            val deviceName: String
        ) : State

        data class Connected(
            val deviceName: String,
            val controllerState: ControllerState
        ) : State

    }

}