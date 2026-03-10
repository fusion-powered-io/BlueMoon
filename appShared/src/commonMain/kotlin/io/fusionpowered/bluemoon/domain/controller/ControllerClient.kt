package io.fusionpowered.bluemoon.domain.controller

import io.fusionpowered.bluemoon.domain.controller.model.ControllerState
import kotlinx.coroutines.flow.StateFlow

interface ControllerClient {

    val controllerStateFlow: StateFlow<ControllerState>

}