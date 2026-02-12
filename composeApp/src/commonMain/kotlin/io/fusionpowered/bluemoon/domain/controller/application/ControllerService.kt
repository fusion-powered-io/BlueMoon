package io.fusionpowered.bluemoon.domain.controller.application

import io.fusionpowered.bluemoon.domain.controller.ControllerClient
import org.koin.core.annotation.Single

@Single
expect class ControllerService() : ControllerClient
