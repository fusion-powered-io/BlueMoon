package io.fusionpowered.bluemoon.presentation.views.controllermode

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import kotlin.time.Duration.Companion.seconds

@Composable
fun presentControllerMode(
    device: BluetoothDevice,
    navigator: Navigator = koinInject(),
    bluetoothConnectionProvider: BluetoothConnectionProvider = koinInject(),
    controllerStateProvider: ControllerStateProvider = koinInject(),
): State {
    LaunchedEffect(bluetoothConnectionProvider) {
        bluetoothConnectionProvider.connect(device)
    }

    val connectionState by bluetoothConnectionProvider.connectionStateFlow.collectAsStateWithLifecycle(ConnectionState.Connecting(device))
    val controllerState by controllerStateProvider.controllerStateFlow.collectAsStateWithLifecycle(ControllerState())
    var countdownJob by remember { mutableStateOf<Job?>(null) }

    LaunchedEffect(controllerState.guide) {
        if (controllerState.guide) {
            countdownJob = launch {
                delay(1.seconds)
                bluetoothConnectionProvider.disconnect(device)
            }
        } else {
            countdownJob?.cancel()
            countdownJob = null
        }
    }

    return when (val it = connectionState) {
        is ConnectionState.Disconnected -> State.Disconnected(
            onDisconnected = { navigator.goBack() }
        )

        is ConnectionState.Connecting -> Connecting(
            deviceName = device.name
        )

        is ConnectionState.Connected -> {
            bluetoothConnectionProvider.send(device, controllerState)
            Connected(
                deviceName = it.device.name,
                controllerState = controllerState
            )
        }
    }
}