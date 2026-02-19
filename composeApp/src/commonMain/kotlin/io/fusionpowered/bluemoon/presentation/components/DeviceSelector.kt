package io.fusionpowered.bluemoon.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.fusionpowered.bluemoon.bootstrap.KoinPresenter
import io.fusionpowered.bluemoon.bootstrap.injectPresenter
import io.fusionpowered.bluemoon.domain.bluetooth.BluetoothClient
import io.fusionpowered.bluemoon.domain.bluetooth.model.BluetoothDevice
import io.fusionpowered.bluemoon.domain.bluetooth.model.BluetoothDevice.MajorClass.*
import io.fusionpowered.bluemoon.bootstrap.PreviewApplication
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Qualifier

object DeviceSelector {

    @Serializable
    data object Screen

    @Qualifier(State::class)
    @Factory
    class Presenter(
        private val bluetoothClient: BluetoothClient,
    ) : KoinPresenter<State> {

        @Composable
        override fun present(): State {
            val availableDevices by bluetoothClient.pairedDevices
                .map {
                    it.filter { device -> device.majorClass in setOf(COMPUTER, PHONE, UNCATEGORIZED) }.toSet()
                }
                .collectAsStateWithLifecycle(emptySet())

            return State(
                availableDevices = availableDevices
            )
        }

    }

    data class State(
        val availableDevices: Set<BluetoothDevice> = emptySet()
    )

    @Composable
    operator fun invoke(
        modifier: Modifier = Modifier,
        presenter: KoinPresenter<State> = injectPresenter<State>()
    ) {
        val state = presenter.present()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 6.dp, bottom = 60.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            state.availableDevices.forEach { device ->
                Device(device)
            }
        }
    }
}

@Preview(
    heightDp = 480,
    widthDp = 640,
)
@Composable
fun DeviceSelectorPreview() =
    PreviewApplication {
        DeviceSelector(
            modifier = Modifier.fillMaxSize(),
        )
    }
