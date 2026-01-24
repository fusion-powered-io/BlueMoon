package io.fusionpowered.bluemoon.domain.bluetooth.mapper

import android.Manifest.permission.BLUETOOTH_CONNECT
import androidx.annotation.RequiresPermission
import io.fusionpowered.bluemoon.domain.bluetooth.model.BluetoothDevice

private typealias AndroidBluetoothDevice = android.bluetooth.BluetoothDevice

@RequiresPermission(BLUETOOTH_CONNECT)
fun AndroidBluetoothDevice.toBluetoothDevice() =
    BluetoothDevice(
        name = name,
        mac = address
    )
