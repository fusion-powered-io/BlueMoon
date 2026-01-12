package io.fusionpowered.bluemoon.BT

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.fusionpowered.bluemoon.Helpers.getParceableExtraSimplified

class BluetoothReceiver(
    private val onDeviceFound: (BluetoothDevice) -> Unit,
    private val onBondStateChanged: () -> Unit
): BroadcastReceiver() {

    override fun onReceive(p0: Context?, intent: Intent?) {
        when(intent?.action) {
            BluetoothDevice.ACTION_FOUND -> {
                val device = intent.getParceableExtraSimplified<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                device?.let(onDeviceFound)
            }
            BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                onBondStateChanged()
            }
        }
    }
}