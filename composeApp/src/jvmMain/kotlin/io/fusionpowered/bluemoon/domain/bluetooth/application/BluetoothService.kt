package io.fusionpowered.bluemoon.domain.bluetooth.application

import io.fusionpowered.bluemoon.domain.bluetooth.BluetoothConnectionProvider
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.logger.Logger

@Single
actual class BluetoothService actual constructor() : KoinComponent, BluetoothConnectionProvider {

    private val logger: Logger by inject()

    override fun send(key: String) {
        logger.info(key)
    }

}