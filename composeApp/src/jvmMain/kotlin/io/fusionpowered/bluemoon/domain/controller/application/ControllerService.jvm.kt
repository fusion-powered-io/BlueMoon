package io.fusionpowered.bluemoon.domain.controller.application

import io.fusionpowered.bluemoon.domain.controller.ControllerClient
import io.fusionpowered.bluemoon.domain.controller.model.ControllerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.annotation.Single

@Single
actual class ControllerService actual constructor() : ControllerClient {

    final override val controllerStateFlow: StateFlow<ControllerState>
        field = MutableStateFlow<ControllerState>(ControllerState())

}