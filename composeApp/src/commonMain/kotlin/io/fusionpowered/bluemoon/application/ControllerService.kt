package io.fusionpowered.bluemoon.application

import io.fusionpowered.bluemoon.ControllerInputHandler
import io.fusionpowered.bluemoon.port.InputSender
import org.koin.core.annotation.Single
import org.koin.core.logger.Logger

@Single
class ControllerService(
    private val logger: Logger,
    private val inputSender: InputSender,
) : ControllerInputHandler {

    override fun handle(keyCode: String) {
        logger.info("In controller")
        inputSender.send(keyCode)
    }

}