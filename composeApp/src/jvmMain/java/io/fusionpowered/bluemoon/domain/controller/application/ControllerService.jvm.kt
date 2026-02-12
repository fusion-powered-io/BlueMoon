package io.fusionpowered.bluemoon.domain.controller.application

import io.fusionpowered.bluemoon.domain.controller.model.ControllerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@org.koin.core.annotation.Single
actual class ControllerService actual constructor() : io.fusionpowered.bluemoon.domain.controller.ControllerClient {

    final override val controllerStateFlow: StateFlow<ControllerState>
        field = MutableStateFlow<ControllerState>(ControllerState())

}