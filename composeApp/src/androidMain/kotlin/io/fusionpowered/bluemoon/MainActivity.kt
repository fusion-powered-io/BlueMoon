package io.fusionpowered.bluemoon

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.fusionpowered.bluemoon.ui.BluetoothViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.compose.viewmodel.koinViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val launcher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) {}

            LaunchedEffect(Unit) {
                launcher.launch(arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_ADVERTISE
                ))
            }

            App({ localAPP() })

        }
    }
}

@Composable
fun localAPP(viewModel: BluetoothViewModel = koinViewModel()) {
    val scanned by viewModel.scannedDevices.collectAsState()
    val paired by viewModel.pairedDevices.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(32.dp)) {
        Text("BlueMoon", style = MaterialTheme.typography.headlineLarge)

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {viewModel.startPairingMode() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Make Phone Visible (Enter pairing mode)")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { viewModel.startScan() }) { Text("Scan") }
            Button(onClick = { viewModel.stopScan() }) { Text("Stop") }
        }

        Text("\nPaired with Desktop:", style = MaterialTheme.typography.titleMedium)
        LazyColumn {
            items(paired) { device ->
                Text(
                    text = "${device.name ?: "Unknown"}",
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        Text("\nNearby Devices:",
            style = MaterialTheme.typography.titleMedium)
        LazyColumn {
            items(scanned) { device ->
                Text("${device.name ?: "Unknown"}", modifier = Modifier.padding(8.dp))
            }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}