package io.fusionpowered.bluemoon.domain.controller.application

import io.fusionpowered.bluemoon.domain.controller.ControllerInputHandler
import io.fusionpowered.bluemoon.domain.bluetooth.BluetoothConnectionProvider
import org.koin.core.annotation.Single
import org.koin.core.logger.Logger

@Single
class ControllerService(
    private val logger: Logger,
    private val bluetoothConnectionProvider: BluetoothConnectionProvider,
) : ControllerInputHandler {

    override fun handle(keyCode: String) {
        logger.info("In controller")
        bluetoothConnectionProvider.send(keyCode)
    }

}