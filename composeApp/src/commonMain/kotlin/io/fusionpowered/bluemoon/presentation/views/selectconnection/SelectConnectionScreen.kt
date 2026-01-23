package io.fusionpowered.bluemoon.presentation.views.selectconnection

import io.fusionpowered.bluemoon.domain.bluetooth.model.BluetoothDevice
import kotlinx.serialization.Serializable

@Serializable
data object SelectConnectionScreen {

    sealed interface State {

        data object NoPairedDevices : State

        data class ShowingDiscoveredDevices(
            val bluetoothDevices: Set<BluetoothDevice> = emptySet(),
            val onDeviceClicked: (bluetoothDevice: BluetoothDevice) -> Unit = {},
        ) : State

    }

}