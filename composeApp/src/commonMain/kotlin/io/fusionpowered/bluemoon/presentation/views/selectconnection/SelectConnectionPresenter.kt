package io.fusionpowered.bluemoon.presentation.views.selectconnection

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.fusionpowered.bluemoon.domain.bluetooth.BluetoothConnectionProvider
import io.fusionpowered.bluemoon.presentation.navigation.Navigator
import io.fusionpowered.bluemoon.presentation.views.controllermode.ControllerModeScreen
import io.fusionpowered.bluemoon.presentation.views.selectconnection.SelectConnectionScreen.State
import org.koin.compose.koinInject


@Composable
fun presentSelectConnection(
    navigator: Navigator = koinInject(),
    bluetoothConnectionProvider: BluetoothConnectionProvider = koinInject(),
): State {
    LaunchedEffect(Unit) {
        bluetoothConnectionProvider.startScanning()
    }
    val savedDevices by bluetoothConnectionProvider.savedDevicesFlow.collectAsStateWithLifecycle(emptySet())
    val scannedDevices by bluetoothConnectionProvider.scannedDevicesFlow.collectAsStateWithLifecycle(emptySet())

    return when {
        scannedDevices.isEmpty() -> State.NoDevices
        else -> State.ShowingDiscoveredDevices(
            savedDevices = savedDevices,
            scannedDevices = scannedDevices - savedDevices,
            onDeviceClicked = { device ->
                navigator.navigateTo(ControllerModeScreen(device))
            }
        )
    }
}

