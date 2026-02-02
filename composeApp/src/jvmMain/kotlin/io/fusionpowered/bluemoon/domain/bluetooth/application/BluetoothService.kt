package io.fusionpowered.bluemoon.domain.bluetooth.application

import io.fusionpowered.bluemoon.domain.bluetooth.BluetoothClient
import io.fusionpowered.bluemoon.domain.bluetooth.model.BluetoothDevice
import io.fusionpowered.bluemoon.domain.bluetooth.model.ConnectionState
import io.fusionpowered.bluemoon.domain.controller.model.ControllerState
import io.fusionpowered.bluemoon.domain.keyboard.model.KeyboardState
import io.fusionpowered.bluemoon.domain.touchpad.model.TouchpadState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.logger.Logger

@Single
actual class BluetoothService actual constructor() : KoinComponent, BluetoothClient {

    private val logger: Logger by inject()

    val mockDevices = setOf(
        BluetoothDevice(
            name = "Macbook Pro",
            mac = "00:00:00:00:00:00",
            majorClass = BluetoothDevice.MajorClass.UNCATEGORIZED
        )
    )

    final override val pairedDevices: StateFlow<Set<BluetoothDevice>>
        field = MutableStateFlow<Set<BluetoothDevice>>(mockDevices)

    final override val connectionStateFlow: StateFlow<ConnectionState>
        field = MutableStateFlow<ConnectionState>(ConnectionState.Connected(mockDevices.first()))


    override suspend fun connect(device: BluetoothDevice) {
        logger.info("Tried to connect to ${device.name}")
    }

    override fun disconnect(device: BluetoothDevice) {

    }

    override fun send(
        device: BluetoothDevice,
        keyboardState: KeyboardState,
    ) {
    }

    override fun send(device: BluetoothDevice, controllerState: ControllerState) {
        logger.info(controllerState.toString())
    }

    override fun send(
        device: BluetoothDevice,
        touchPadState: TouchpadState,
    ) {

    }


}