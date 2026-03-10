package io.fusionpowered.bluemoon.domain.bluetooth.model


sealed interface ConnectionState {

    data object Disconnected : ConnectionState

    data class Connecting(val device: BluetoothDevice) : ConnectionState

    data class Connected(val device: BluetoothDevice) : ConnectionState

}