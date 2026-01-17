package io.fusionpowered.bluemoon.domain.bluetooth.mapper

import android.Manifest
import android.bluetooth.BluetoothDevice
import androidx.annotation.RequiresPermission
import io.fusionpowered.bluemoon.domain.bluetooth.model.PairedDevice

@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
fun BluetoothDevice.toPairedDevice() =
    PairedDevice(
        name = name,
        mac = address
    )