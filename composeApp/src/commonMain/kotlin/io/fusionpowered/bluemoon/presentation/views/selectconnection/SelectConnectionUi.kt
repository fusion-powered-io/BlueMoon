package io.fusionpowered.bluemoon.presentation.views.selectconnection

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.fusionpowered.bluemoon.domain.bluetooth.model.BluetoothDevice
import io.fusionpowered.bluemoon.presentation.views.selectconnection.SelectConnectionScreen.State


@Composable
fun SelectConnectionUi(
    state: State,
) {
    when (state) {
        is State.NoDevices -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "No Connections",
                    modifier = Modifier.offset(y = (-100).dp),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        is State.ShowingDiscoveredDevices -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Select Connection")
                Column(
                    modifier = Modifier
                        .padding(56.dp)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = spacedBy(24.dp, alignment = Alignment.Top),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Saved Devices")
                    for (device in state.savedDevices) {
                        Button(
                            onClick = { state.onDeviceClicked(device) },
                        ) {
                            Column {
                                Text(device.name)
                            }
                        }
                    }
                    Text("Scanned Devices")
                    for (device in state.scannedDevices) {
                        Button(
                            onClick = { state.onDeviceClicked(device) },
                        ) {
                            Column {
                                Text(device.name)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun SelectConnectionUiPreview() {
    SelectConnectionUi(
        state = State.ShowingDiscoveredDevices(
            savedDevices = setOf(
                BluetoothDevice(
                    name = "test device 1",
                    mac = "00:00:00:00:00:01"
                )
            ),
            scannedDevices = setOf(
                BluetoothDevice(
                    name = "test device 2",
                    mac = "00:00:00:00:00:01"
                ),
                BluetoothDevice(
                    name = "test device 3",
                    mac = "00:00:00:00:00:02"
                )
            )
        )
    )
}