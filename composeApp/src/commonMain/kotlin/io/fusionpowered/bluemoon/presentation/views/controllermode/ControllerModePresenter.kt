package io.fusionpowered.bluemoon.presentation.views.controllermode

import androidx.compose.runtime.Composable
import io.fusionpowered.bluemoon.domain.bluetooth.model.PairedDevice
import io.fusionpowered.bluemoon.presentation.views.controllermode.ControllerModeScreen.State

@Composable
fun presentControllerMode(
    device: PairedDevice,
): State {
    return State.Connecting(deviceName = device.name)
}