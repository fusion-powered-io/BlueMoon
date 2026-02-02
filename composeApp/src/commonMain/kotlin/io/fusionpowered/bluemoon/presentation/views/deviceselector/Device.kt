package io.fusionpowered.bluemoon.presentation.views.deviceselector

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import compose.icons.TablerIcons
import compose.icons.tablericons.Bluetooth
import compose.icons.tablericons.Circle
import compose.icons.tablericons.CircleCheck
import compose.icons.tablericons.CircleDotted
import compose.icons.tablericons.DeviceLaptop
import compose.icons.tablericons.DeviceMobile
import io.fusionpowered.bluemoon.domain.bluetooth.BluetoothClient
import io.fusionpowered.bluemoon.domain.bluetooth.model.BluetoothDevice
import io.fusionpowered.bluemoon.domain.bluetooth.model.BluetoothDevice.MajorClass.COMPUTER
import io.fusionpowered.bluemoon.domain.bluetooth.model.BluetoothDevice.MajorClass.PHONE
import io.fusionpowered.bluemoon.domain.bluetooth.model.ConnectionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

object Device {

    @Composable
    fun present(
        device: BluetoothDevice,
        bluetoothClient: BluetoothClient = koinInject(),
        coroutineScope: CoroutineScope = rememberCoroutineScope(),
    ): State {
        val connectionState by bluetoothClient.connectionStateFlow
            .collectAsStateWithLifecycle(ConnectionState.Disconnected)

        return when (val it = connectionState) {
            is ConnectionState.Connecting if (it.device == device) -> {
                State.Connecting(
                    device = it.device,
                    onClicked = { bluetoothClient.disconnect(device) }
                )
            }

            is ConnectionState.Connected if (it.device == device) -> {
                State.Connected(
                    device = it.device,
                    onClicked = { bluetoothClient.disconnect(device) }
                )
            }

            is ConnectionState.Connected if (it.device != device) -> {
                State.Disconnected(
                    device = device,
                    onClicked = {
                        bluetoothClient.disconnect(it.device)
                        coroutineScope.launch {
                            delay(500) // Give it some time to disconnect
                            bluetoothClient.connect(device)
                        }
                    }
                )
            }

            else -> {
                State.Disconnected(
                    device = device,
                    onClicked = {
                        coroutineScope.launch {
                            bluetoothClient.connect(device)
                        }
                    }
                )
            }
        }
    }

    sealed interface State {

        val device: BluetoothDevice

        data class Disconnected(
            override val device: BluetoothDevice,
            val onClicked: () -> Unit = {},
        ) : State

        data class Connecting(
            override val device: BluetoothDevice,
            val onClicked: () -> Unit = {},
        ) : State

        data class Connected(
            override val device: BluetoothDevice,
            val onClicked: () -> Unit = {},
        ) : State

    }

    @Composable
    operator fun invoke(
        modifier: Modifier = Modifier,
        presenter: @Composable () -> State,
    ) {
        val state = presenter()
        Surface(
            onClick = {
                when (state) {
                    is State.Disconnected -> state.onClicked()
                    is State.Connecting -> state.onClicked()
                    is State.Connected -> state.onClicked()
                }
            },
            modifier = modifier.fillMaxWidth(),
            color = Color.Transparent
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (state.device.majorClass) {
                            COMPUTER -> TablerIcons.DeviceLaptop
                            PHONE -> TablerIcons.DeviceMobile
                            else -> TablerIcons.Bluetooth
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = state.device.name,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = state.device.mac,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = when (state) {
                            is State.Disconnected -> "Disconnected"
                            is State.Connecting -> "Connecting"
                            is State.Connected -> "Connected"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = when (state) {
                            is State.Disconnected -> TablerIcons.Circle
                            is State.Connecting -> TablerIcons.CircleDotted
                            is State.Connected -> TablerIcons.CircleCheck
                        },
                        contentDescription = null,
                        tint = when (state) {
                            is State.Disconnected -> Color.DarkGray
                            is State.Connecting -> Color.DarkGray
                            is State.Connected -> Color.Green
                        }
                    )
                }

            }

        }
    }

}

@Preview
@Composable
fun DevicePreview() {
    Device(
        presenter = {
            Device.State.Connected(
                device = BluetoothDevice(
                    name = "test device",
                    mac = "00:00:00:00:00:00",
                    majorClass = COMPUTER
                )
            )
        }
    )
}