package io.fusionpowered.bluemoon.presentation.views.controllermode

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.fusionpowered.bluemoon.domain.bluetooth.BluetoothConnectionProvider
import io.fusionpowered.bluemoon.domain.bluetooth.model.BluetoothDevice
import io.fusionpowered.bluemoon.domain.bluetooth.model.ConnectionState
import io.fusionpowered.bluemoon.domain.controller.ControllerStateProvider
import io.fusionpowered.bluemoon.domain.controller.model.ControllerState
import io.fusionpowered.bluemoon.presentation.navigation.Navigator
import io.fusionpowered.bluemoon.presentation.views.controllermode.ControllerModeScreen.State
import io.fusionpowered.bluemoon.presentation.views.controllermode.ControllerModeScreen.State.Connected
import io.fusionpowered.bluemoon.presentation.views.controllermode.ControllerModeScreen.State.Connecting
import io.fusionpowered.bluemoon.presentation.views.controllermode.ControllerModeScreen.State.Disconnected
import org.koin.compose.koinInject
import org.koin.core.logger.Logger

@Composable
fun presentControllerMode(
    device: BluetoothDevice,
    logger: Logger = koinInject(),
    navigator: Navigator = koinInject(),
    bluetoothConnectionProvider: BluetoothConnectionProvider = koinInject(),
    controllerStateProvider: ControllerStateProvider = koinInject(),
): State {
    LaunchedEffect(Unit) {
        bluetoothConnectionProvider.connect(device)
    }

    val connectionState by bluetoothConnectionProvider.connectionStateFlow.collectAsStateWithLifecycle(ConnectionState.Disconnected)
    val controllerState by controllerStateProvider.controllerStateFlow.collectAsStateWithLifecycle(ControllerState())

    LaunchedEffect(controllerState.guide) {
        if (controllerState.guide) {
            navigator.goBack()
        }
    }

    return when (val it = connectionState) {
        is ConnectionState.Disconnected -> Disconnected

        is ConnectionState.Connecting -> Connecting(it.device.name)

        is ConnectionState.Connected -> {
            bluetoothConnectionProvider.send(controllerState)
            Connected(
                deviceName = it.device.name,
                controllerState = controllerState
            )
        }
    }
}