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
    val pairedDevices by bluetoothConnectionProvider.pairedDevicesFlow.collectAsStateWithLifecycle(emptySet())

    return when {
        pairedDevices.isEmpty() -> State.NoPairedDevices
        else -> State.ShowingDiscoveredDevices(
            bluetoothDevices = pairedDevices,
            onDeviceClicked = { device ->
                navigator.navigateTo(ControllerModeScreen(device))
            }
        )
    }
}

