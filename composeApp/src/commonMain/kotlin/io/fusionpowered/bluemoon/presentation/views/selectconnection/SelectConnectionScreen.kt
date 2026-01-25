package io.fusionpowered.bluemoon.presentation.views.selectconnection

import io.fusionpowered.bluemoon.domain.bluetooth.model.BluetoothDevice
import kotlinx.serialization.Serializable

@Serializable
data object SelectConnectionScreen {

    sealed interface State {

        data object NoDevices : State

        data class ShowingDiscoveredDevices(
            val savedDevices: Set<BluetoothDevice> = emptySet(),
            val scannedDevices: Set<BluetoothDevice> = emptySet(),
            val onDeviceClicked: (bluetoothDevice: BluetoothDevice) -> Unit = {},
        ) : State

    }

}