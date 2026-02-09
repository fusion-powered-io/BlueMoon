package io.fusionpowered.bluemoon.domain.bluetooth

import io.fusionpowered.bluemoon.domain.bluetooth.model.BluetoothDevice
import io.fusionpowered.bluemoon.domain.bluetooth.model.ConnectionState
import io.fusionpowered.bluemoon.domain.controller.model.ControllerState
import io.fusionpowered.bluemoon.domain.keyboard.model.KeyboardState
import io.fusionpowered.bluemoon.domain.touchpad.model.TouchpadState
import kotlinx.coroutines.flow.StateFlow

interface BluetoothClient {

    val pairedDevices: StateFlow<Set<BluetoothDevice>>

    val connectionStateFlow: StateFlow<ConnectionState>

    suspend fun connect(device: BluetoothDevice)

    fun disconnect(device: BluetoothDevice)

    fun send(device: BluetoothDevice, keyboardState: KeyboardState)

    fun send(device: BluetoothDevice, touchPadState: TouchpadState)

    fun send(device: BluetoothDevice, controllerState: ControllerState)

}