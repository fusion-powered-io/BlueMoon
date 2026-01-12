package io.fusionpowered.bluemoon.ui

import androidx.lifecycle.ViewModel
import io.fusionpowered.bluemoon.domain.BluetoothController
import io.fusionpowered.bluemoon.domain.BluetoothDevice

class BluetoothViewModel (
    private val bluetoothController: BluetoothController
) : ViewModel() {
    val scannedDevices = bluetoothController.scannedDevices
    val pairedDevices = bluetoothController.pairedDevices

    fun startPairingMode() = bluetoothController.startPairingMode()
    fun startScan() = bluetoothController.startDiscovery()
    fun stopScan() = bluetoothController.stopDiscovery()
    fun onDeviceClick(device: BluetoothDevice) = bluetoothController.connectToDevice(device)

}