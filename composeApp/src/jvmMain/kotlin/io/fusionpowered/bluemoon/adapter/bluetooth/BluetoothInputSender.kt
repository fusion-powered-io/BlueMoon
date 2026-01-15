package io.fusionpowered.bluemoon.adapter.bluetooth

import io.fusionpowered.bluemoon.port.InputSender
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.logger.Logger

@Single
actual class BluetoothInputSender actual constructor() : KoinComponent, InputSender {

    private val logger: Logger by inject()

    override fun send(key: String) {
        logger.info(key)
    }

}