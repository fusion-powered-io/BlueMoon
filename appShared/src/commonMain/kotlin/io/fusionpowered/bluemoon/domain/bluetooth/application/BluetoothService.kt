package io.fusionpowered.bluemoon.domain.bluetooth.application

import io.fusionpowered.bluemoon.domain.bluetooth.BluetoothManager
import io.fusionpowered.bluemoon.domain.bluetooth.model.BluetoothDevice
import io.fusionpowered.bluemoon.domain.bluetooth.port.BluetoothClient
import io.fusionpowered.bluemoon.domain.bluetooth.port.BluetoothSettingsLauncher
import io.fusionpowered.bluemoon.domain.controller.model.ControllerState
import io.fusionpowered.bluemoon.domain.keyboard.model.KeyboardState
import io.fusionpowered.bluemoon.domain.touchpad.model.TouchpadState
import io.fusionpowered.bluemoon.domain.volume.model.VolumeState
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Single
class BluetoothService: BluetoothManager, KoinComponent {

    private val bluetoothClient: BluetoothClient by inject()
    private val bluetoothSettingsLauncher: BluetoothSettingsLauncher by inject()

    override val pairedDevices = bluetoothClient.pairedDevices

    override val connectionStateFlow = bluetoothClient.connectionStateFlow

    override suspend fun connect(device: BluetoothDevice) {
        bluetoothClient.connect(device)
    }

    override fun disconnect(device: BluetoothDevice) {
        bluetoothClient.disconnect(device)
    }

    override fun send(
        device: BluetoothDevice,
        controllerState: ControllerState,
    ) {
       bluetoothClient.send(device, controllerState)
    }

    override fun send(
        device: BluetoothDevice,
        keyboardState: KeyboardState,
    ) {
        bluetoothClient.send(device, keyboardState)
    }

    override fun send(
        device: BluetoothDevice,
        touchPadState: TouchpadState,
    ) {
        bluetoothClient.send(device, touchPadState)
    }

    override fun send(
        device: BluetoothDevice,
        volumeState: VolumeState,
    ) {
        bluetoothClient.send(device, volumeState)
    }

    override fun launchBluetoothSettings() {
        bluetoothSettingsLauncher.launch()
    }

}