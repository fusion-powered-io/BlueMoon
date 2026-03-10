package io.fusionpowered.bluemoon.domain.bluetooth.mapper

import android.bluetooth.BluetoothClass
import io.fusionpowered.bluemoon.domain.bluetooth.model.BluetoothDevice

fun Int.toMajorClass() =
    when (this) {
        BluetoothClass.Device.Major.COMPUTER -> BluetoothDevice.MajorClass.COMPUTER
        BluetoothClass.Device.Major.PHONE -> BluetoothDevice.MajorClass.PHONE
        BluetoothClass.Device.Major.NETWORKING -> BluetoothDevice.MajorClass.NETWORKING
        BluetoothClass.Device.Major.AUDIO_VIDEO -> BluetoothDevice.MajorClass.AUDIO_VIDEO
        BluetoothClass.Device.Major.PERIPHERAL -> BluetoothDevice.MajorClass.PERIPHERAL
        BluetoothClass.Device.Major.IMAGING -> BluetoothDevice.MajorClass.IMAGING
        BluetoothClass.Device.Major.WEARABLE -> BluetoothDevice.MajorClass.WEARABLE
        BluetoothClass.Device.Major.TOY -> BluetoothDevice.MajorClass.TOY
        BluetoothClass.Device.Major.HEALTH -> BluetoothDevice.MajorClass.HEALTH
        else -> BluetoothDevice.MajorClass.UNCATEGORIZED
    }
