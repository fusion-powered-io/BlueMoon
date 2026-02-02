package io.fusionpowered.bluemoon.presentation.views.deviceselector

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import org.koin.compose.koinInject

object DeviceSelector {

    @Serializable
    data object Screen

    @Composable
    fun present(
        bluetoothClient: BluetoothClient = koinInject(),
        bluetoothSettings: BluetoothSettings = koinInject(),
    ): State {
        val availableDevices by bluetoothClient.pairedDevices
            .map {
                it.filter { device -> device.majorClass in setOf(COMPUTER, PHONE, UNCATEGORIZED) }.toSet()
            }
            .collectAsStateWithLifecycle(emptySet())

        return State(
            availableDevices = availableDevices,
            onPairNewDevice = { bluetoothSettings.launch() },
            devicePresenter = { device -> Device.present(device) }
        )
    }

    data class State(
        val availableDevices: Set<BluetoothDevice> = emptySet(),
        val onPairNewDevice: () -> Unit = {},
        val devicePresenter: @Composable (bluetoothDevice: BluetoothDevice) -> Device.State,
    )

    @Composable
    operator fun invoke(
        modifier: Modifier = Modifier,
        presenter: @Composable () -> State = ::present,
    ) =
        Box(modifier = modifier.fillMaxSize()) {
            val state = presenter()

            val bottomSheetSize = remember { 60.dp }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = bottomSheetSize)
            ) {
                PairNewDeviceButton(state.onPairNewDevice)
                state.availableDevices.forEachIndexed { index, device ->
                    Device(
                        presenter = { state.devicePresenter(device) }
                    )

                    // The separator logic stays the same
                    if (index < state.availableDevices.size) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(bottomSheetSize)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, MaterialTheme.colorScheme.surface)
                        )
                    )
            )
        }
}

@Composable
fun PairNewDeviceButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .height(56.dp), // Matches the height shown in the render
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.5.dp, Color(0xFF4A90E2).copy(alpha = 0.6f)),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Color(0xFF4A90E2)
        ),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start, // Aligns content like the image
            modifier = Modifier.fillMaxWidth()
        ) {
            // The Circular Plus Icon
            Surface(
                shape = CircleShape,
                color = Color.Transparent,
                border = BorderStroke(1.5.dp, Color(0xFF4A90E2)),
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = TablerIcons.CirclePlus,
                    contentDescription = null,
                    modifier = Modifier.padding(2.dp),
                    tint = Color(0xFF4A90E2)
                )
            }

            Spacer(Modifier.width(24.dp)) // Large gap as seen in the UI render

            Text(
                text = "Pair New Device",
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp
                )
            )
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