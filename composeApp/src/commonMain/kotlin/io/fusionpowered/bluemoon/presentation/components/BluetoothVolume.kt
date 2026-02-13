package io.fusionpowered.bluemoon.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.fusionpowered.bluemoon.domain.bluetooth.BluetoothClient
import io.fusionpowered.bluemoon.domain.bluetooth.model.ConnectionState
import io.fusionpowered.bluemoon.domain.volume.VolumeClient
import org.koin.compose.koinInject


object BluetoothVolume {

    @Composable
    fun present(
        volumeClient: VolumeClient = koinInject(),
        bluetoothClient: BluetoothClient = koinInject(),
    ): State {
        val connectionState by bluetoothClient.connectionStateFlow.collectAsStateWithLifecycle()
        val controllerState by volumeClient.volumeStateFlow.collectAsStateWithLifecycle()

        when (val connection = connectionState) {
            is ConnectionState.Connected -> bluetoothClient.send(connection.device, controllerState)
            else -> {}
        }

        return State
    }

    data object State

}
