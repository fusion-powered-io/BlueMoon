package io.fusionpowered.bluemoon.domain.bluetooth.application

import io.fusionpowered.bluemoon.domain.bluetooth.BluetoothConnectionProvider
import io.fusionpowered.bluemoon.domain.bluetooth.model.BluetoothDevice
import io.fusionpowered.bluemoon.domain.bluetooth.model.ConnectionState
import io.fusionpowered.bluemoon.domain.controller.model.ControllerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.logger.Logger

@Single
actual class BluetoothService actual constructor() : KoinComponent, BluetoothConnectionProvider {

    private val logger: Logger by inject()


    final override val scannedDevicesFlow: StateFlow<Set<BluetoothDevice>>
        field = MutableStateFlow<Set<BluetoothDevice>>(emptySet())

    final override val connectionStateFlow: StateFlow<ConnectionState>
        field = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)


    override fun connect(device: BluetoothDevice) {
        logger.info("Tried to connect to ${device.name}")
    }

    override fun send(controllerState: ControllerState) {
        logger.info(controllerState.toString())
    }

    override fun startScanning() {
        logger.info("TODO: implement scanning")
    }

}