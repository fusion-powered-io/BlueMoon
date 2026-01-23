package io.fusionpowered.bluemoon.domain.controller.application

import io.fusionpowered.bluemoon.domain.controller.ControllerStateProvider
import org.koin.core.annotation.Single

@Single
expect class ControllerService() : ControllerStateProvider
