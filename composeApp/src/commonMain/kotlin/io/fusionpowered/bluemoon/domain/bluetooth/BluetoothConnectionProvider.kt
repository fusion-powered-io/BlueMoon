package io.fusionpowered.bluemoon.domain.bluetooth

import io.fusionpowered.bluemoon.domain.bluetooth.model.BluetoothDevice
import io.fusionpowered.bluemoon.domain.bluetooth.model.ConnectionState
import io.fusionpowered.bluemoon.domain.controller.model.ControllerState
import kotlinx.coroutines.flow.StateFlow

interface BluetoothConnectionProvider {

    val scannedDevicesFlow: StateFlow<Set<BluetoothDevice>>

    val connectionStateFlow: StateFlow<ConnectionState>

    fun connect(device: BluetoothDevice)

    fun disconnect()

    fun send(controllerState: ControllerState)

    fun startScanning()

}