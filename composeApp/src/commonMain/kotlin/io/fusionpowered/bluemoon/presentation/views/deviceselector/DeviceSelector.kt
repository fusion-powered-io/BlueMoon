package io.fusionpowered.bluemoon.presentation.views.deviceselector

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bluemoon.composeapp.generated.resources.Res
import bluemoon.composeapp.generated.resources.background
import compose.icons.TablerIcons
import compose.icons.tablericons.CirclePlus
import io.fusionpowered.bluemoon.domain.bluetooth.BluetoothClient
import io.fusionpowered.bluemoon.domain.bluetooth.BluetoothSettings
import io.fusionpowered.bluemoon.domain.bluetooth.model.BluetoothDevice
import io.fusionpowered.bluemoon.domain.bluetooth.model.BluetoothDevice.MajorClass.COMPUTER
import io.fusionpowered.bluemoon.domain.bluetooth.model.BluetoothDevice.MajorClass.PHONE
import io.fusionpowered.bluemoon.domain.bluetooth.model.BluetoothDevice.MajorClass.UNCATEGORIZED
import io.fusionpowered.bluemoon.presentation.theme.BlueMoonTheme
import io.fusionpowered.bluemoon.presentation.views.deviceselector.DeviceSelector.State
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject

object DeviceSelector {

    @Serializable
    data object Screen

    @Composable
    fun present(
        bluetoothClient: BluetoothClient = koinInject(),
    ): State {
        val availableDevices by bluetoothClient.pairedDevices
            .map {
                it.filter { device -> device.majorClass in setOf(COMPUTER, PHONE, UNCATEGORIZED) }.toSet()
            }
            .collectAsStateWithLifecycle(emptySet())

        return State(
            availableDevices = availableDevices,
            devicePresenter = { device -> Device.present(device) }
        )
    }

    data class State(
        val availableDevices: Set<BluetoothDevice> = emptySet(),
        val devicePresenter: @Composable (bluetoothDevice: BluetoothDevice) -> Device.State,
    )

    @Composable
    operator fun invoke(
        modifier: Modifier = Modifier,
        presenter: @Composable () -> State = ::present,
    ) {
        val state = presenter()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 6.dp, bottom = 60.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            state.availableDevices.forEach { device ->
                Device(presenter = { state.devicePresenter(device) })
            }
        }
    }
}

@Preview(
    heightDp = 480,
    widthDp = 640,
    showBackground = true
)
@Composable
fun DeviceSelectorPreview() =
    BlueMoonTheme {
        DeviceSelector(
            modifier = Modifier
                .fillMaxSize(),
            presenter = {
                State(
                    availableDevices = setOf(
                        BluetoothDevice(
                            name = "test device 2",
                            mac = "00:00:00:00:00:01",
                            majorClass = COMPUTER
                        ),
                        BluetoothDevice(
                            name = "test device 3",
                            mac = "00:00:00:00:00:02",
                            majorClass = PHONE
                        )
                    ),
                    devicePresenter = { device ->
                        Device.State.Connected(device)
                    }
                )
            }
        )
    }