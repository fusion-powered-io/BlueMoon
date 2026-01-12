package io.fusionpowered.bluemoon.BT

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.util.Log
import io.fusionpowered.bluemoon.domain.BluetoothController
import io.fusionpowered.bluemoon.domain.BluetoothDeviceDomain
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.UUID

@SuppressLint("MissingPermission")
class AndroidBluetoothController(
    private val context: Context
) : BluetoothController {

    companion object {
        const val BT_UUID = "00001101-0000-1000-8000-00805F9B34FB" // according to a ran spec said we should define a UUID.
        private var serverSocket: BluetoothServerSocket? = null
    }

    private val bluetoothManager by lazy {
        context.getSystemService(BluetoothManager::class.java) as BluetoothManager
    }

    private val bluetoothAdapter by lazy {
        bluetoothManager?.adapter as BluetoothAdapter
    }

    private val _scannedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())
    override val scannedDevices: StateFlow<List<BluetoothDeviceDomain>>
        get() = _scannedDevices.asStateFlow()

    private val _pairedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())
    override val pairedDevices: StateFlow<List<BluetoothDeviceDomain>>
        get() =  _pairedDevices.asStateFlow()


    private val mBluetoothReceiver = BluetoothReceiver(
        onDeviceFound = { device ->
            _scannedDevices.update { devices ->
                val newDevice = device.toBluetoothDeviceDomain()
                if (newDevice in devices) devices else devices + newDevice
            }
        },
        onBondStateChanged = {
            updatePairedDevices()
        })

    override fun startDiscovery() {
        if(!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            return
        }

        try { context.unregisterReceiver(mBluetoothReceiver) } catch (e: Exception) {}

        context.registerReceiver(
            mBluetoothReceiver,
            IntentFilter().apply {
                addAction(BluetoothDevice.ACTION_FOUND)
                addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
            }

        )

        updatePairedDevices()

        bluetoothAdapter?.cancelDiscovery()
        bluetoothAdapter?.startDiscovery()
    }

    override fun stopDiscovery() {
        if(!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            return
        }

        bluetoothAdapter?.cancelDiscovery()
    }

    override fun connectToDevice(device: BluetoothDeviceDomain) {
        val androidDevice = bluetoothAdapter?.getRemoteDevice(device.address)

        if (androidDevice?.bondState == BluetoothDevice.BOND_NONE) {
            androidDevice.createBond()
            return
        }

        val uuid = UUID.fromString(BT_UUID)
        CoroutineScope(Dispatchers.IO).launch {
            var socket: BluetoothSocket? = null

            try {
                socket = androidDevice?.createRfcommSocketToServiceRecord(uuid)
                socket?.connect()
            } catch (e : IOException) {
                socket?.close()
            }
        }

    }

    override fun startPairingMode() {
        val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
            putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(discoverableIntent)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                serverSocket = bluetoothAdapter?.listenUsingRfcommWithServiceRecord(
                    "BlueMoon",
                    UUID.fromString(BT_UUID)
                )

                Log.d("BT_SERVER", "Waiting for connection...")
                val socket = serverSocket?.accept() // This blocks until a device connects

                socket?.let {
                    Log.d("BT_SERVER", "Connected to: ${socket.remoteDevice.name}")
                    serverSocket?.close()
                }

            } catch (e: IOException) {
                Log.e("BT_SERVER", "Socket accept failed", e)
            }
        }
        updatePairedDevices()
    }

    override fun release() {
        context.unregisterReceiver(mBluetoothReceiver)
    }

    fun updatePairedDevices() {
        if(!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            return
        }

        val devices = bluetoothAdapter?.bondedDevices
            ?.map { it.toBluetoothDeviceDomain() }
            ?: emptyList()

        _pairedDevices.update { devices }

    }

    private fun hasPermission(permission: String): Boolean {
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }
}