package io.fusionpowered.bluemoon.presentation.component.headless

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.fusionpowered.bluemoon.bootstrap.KoinPresenter
import io.fusionpowered.bluemoon.domain.bluetooth.BluetoothManager
import io.fusionpowered.bluemoon.domain.bluetooth.model.ConnectionState
import io.fusionpowered.bluemoon.domain.volume.VolumeClient
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.Qualifier

object BluetoothVolume {

    @Qualifier(State::class)
    @KoinViewModel
    class Presenter(
        private val volumeClient: VolumeClient,
        private val bluetoothManager: BluetoothManager,
    ) : KoinPresenter<State>() {

        @Composable
        override fun present(): State {
            val connectionState by bluetoothManager.connectionStateFlow.collectAsStateWithLifecycle()
            val controllerState by volumeClient.volumeStateFlow.collectAsStateWithLifecycle()

            when (val connection = connectionState) {
                is ConnectionState.Connected -> bluetoothManager.send(connection.device, controllerState)
                else -> {}
            }

            return State
        }

    }

    data object State

}