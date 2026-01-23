package io.fusionpowered.bluemoon.domain.bluetooth.mapper

import android.annotation.SuppressLint
import io.fusionpowered.bluemoon.domain.bluetooth.model.BluetoothDevice

private typealias AndroidBluetoothDevice = android.bluetooth.BluetoothDevice

@SuppressLint("MissingPermission")
fun AndroidBluetoothDevice.toBluetoothDevice() =
    BluetoothDevice(
        name = name,
        mac = address
    )
