package io.fusionpowered.bluemoon.presentation.page

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bluemoon.appshared.generated.resources.Res
import bluemoon.appshared.generated.resources.background
import io.fusionpowered.bluemoon.bootstrap.KoinPresenter
import io.fusionpowered.bluemoon.bootstrap.PreviewApplication
import io.fusionpowered.bluemoon.bootstrap.injectPresenter
import io.fusionpowered.bluemoon.domain.bluetooth.BluetoothManager
import io.fusionpowered.bluemoon.domain.bluetooth.model.BluetoothDevice
import io.fusionpowered.bluemoon.domain.bluetooth.model.BluetoothDevice.MajorClass.COMPUTER
import io.fusionpowered.bluemoon.domain.bluetooth.model.BluetoothDevice.MajorClass.PHONE
import io.fusionpowered.bluemoon.domain.bluetooth.model.BluetoothDevice.MajorClass.UNCATEGORIZED
import io.fusionpowered.bluemoon.presentation.component.headless.BluetoothController
import io.fusionpowered.bluemoon.presentation.component.headless.BluetoothVolume
import io.fusionpowered.bluemoon.presentation.component.layout.HomeLayout
import io.fusionpowered.bluemoon.presentation.component.widget.DeviceCard
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.painterResource
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.Qualifier

object Home {

    @Serializable
    data object Screen

    @Qualifier(State::class)
    @KoinViewModel
    class Presenter(
        private val bluetoothManager: BluetoothManager,
    ) : KoinPresenter<State>() {

        @Composable
        override fun present(): State {
            val availableDevices by bluetoothManager.pairedDevices
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
        val availableDevices: Set<BluetoothDevice> = emptySet(),
    )

    @Composable
    operator fun invoke(
        modifier: Modifier = Modifier,
        presenter: KoinPresenter<State> = injectPresenter<State>(),
        bluetoothControllerPresenter: KoinPresenter<BluetoothController.State> = injectPresenter<BluetoothController.State>(),
        bluetoothVolumePresenter: KoinPresenter<BluetoothVolume.State> = injectPresenter<BluetoothVolume.State>(),
    ) {
        bluetoothControllerPresenter.present()
        bluetoothVolumePresenter.present()

        Background()
        HomeLayout {
            val state = presenter.present()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 6.dp, bottom = 60.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                state.availableDevices.forEach { device ->
                    DeviceCard(device)
                }
            }
        }
    }

    @Composable
    private fun Background() {
        Image(
            painter = painterResource(Res.drawable.background),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            contentDescription = null
        )
    }

}

@Preview(
    heightDp = 480,
    widthDp = 640,
)
@Composable
fun DeviceSelectorPreview() =
    PreviewApplication {
        Home(
            modifier = Modifier.fillMaxSize(),
        )
    }
