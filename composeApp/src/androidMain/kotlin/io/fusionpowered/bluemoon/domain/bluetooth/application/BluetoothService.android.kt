package io.fusionpowered.bluemoon.domain.bluetooth.application

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice.EXTRA_DEVICE
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothHidDeviceAppSdpSettings
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import io.fusionpowered.bluemoon.domain.bluetooth.BluetoothConnectionProvider
import io.fusionpowered.bluemoon.domain.bluetooth.mapper.toBluetoothDevice
import io.fusionpowered.bluemoon.domain.bluetooth.model.BluetoothDevice
import io.fusionpowered.bluemoon.domain.bluetooth.model.ConnectionState
import io.fusionpowered.bluemoon.domain.controller.model.ControllerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.logger.Logger
import java.util.concurrent.Executors

private typealias AndroidBluetoothDevice = android.bluetooth.BluetoothDevice

@SuppressLint("MissingPermission")
@Single
actual class BluetoothService actual constructor() : KoinComponent, BluetoothConnectionProvider {

    private val applicationContext by inject<Context>()
    private val logger by inject<Logger>()
    private val manager = applicationContext.getSystemService(BluetoothManager::class.java)
    private val adapter = checkNotNull(manager.adapter)

    var pairedAndroidDevices = mutableSetOf<AndroidBluetoothDevice>()

    final override val pairedDevicesFlow: StateFlow<Set<BluetoothDevice>>
        field = MutableStateFlow(emptySet())


    final override val connectionStateFlow: StateFlow<ConnectionState>
        field = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)

    private var hidDevice: BluetoothHidDevice? = null
    private var connectedDevice: AndroidBluetoothDevice? = null

    @OptIn(ExperimentalUnsignedTypes::class)
    private val serviceDiscoveryProtocolSettings = BluetoothHidDeviceAppSdpSettings(
        "BlueMoon Gamepad",
        "BlueMoon HID Controller",
        "Fusion",
        BluetoothHidDevice.SUBCLASS1_COMBO,
        ubyteArrayOf(
            0x05u, 0x01u, 0x09u, 0x05u, 0xA1u, 0x01u, 0xA1u, 0x00u,
            // Sticks: 4 x 16-bit (Bytes 0-7)
            0x09u, 0x30u, 0x09u, 0x31u, 0x09u, 0x33u, 0x09u, 0x34u,
            0x15u, 0x00u, 0x26u, 0xFFu, 0xFFu, 0x35u, 0x00u, 0x46u, 0xFFu, 0xFFu,
            0x95u, 0x04u, 0x75u, 0x10u, 0x81u, 0x02u, 0xC0u,
            // Triggers: 2 x 8-bit (Bytes 8-9)
            0x05u, 0x01u, 0x09u, 0x32u, 0x09u, 0x35u,
            0x15u, 0x00u, 0x26u, 0xFFu, 0x00u, 0x95u, 0x02u, 0x75u, 0x08u, 0x81u, 0x02u,
            // Buttons: 11 buttons (Byte 10 + part of Byte 11)
            0x05u, 0x09u, 0x19u, 0x01u, 0x29u, 0x0Bu,
            0x15u, 0x00u, 0x25u, 0x01u, 0x95u, 0x0Bu, 0x75u, 0x01u, 0x81u, 0x02u,
            // Padding to finish Byte 11 (5 bits)
            0x95u, 0x01u, 0x75u, 0x05u, 0x81u, 0x03u,
            // Hat Switch: 1 x 4-bit (Byte 12)
            0x05u, 0x01u, 0x09u, 0x39u, 0x15u, 0x01u, 0x25u, 0x08u, 0x35u, 0x00u, 0x46u, 0x3Bu, 0x10u,
            0x66u, 0x0Eu, 0x00u, 0x95u, 0x01u, 0x75u, 0x04u, 0x81u, 0x42u,
            // Final Padding to finish Byte 12 (4 bits)
            0x95u, 0x01u, 0x75u, 0x04u, 0x81u, 0x03u,
            // End (Total 13 bytes)
            0xC0u
        ).toByteArray()
    )

    private val callback = object : BluetoothHidDevice.Callback() {
        override fun onConnectionStateChanged(device: AndroidBluetoothDevice?, state: Int) {
            if (state == BluetoothProfile.STATE_CONNECTED) {
                connectedDevice = device
            } else if (state == BluetoothProfile.STATE_DISCONNECTED) {
                connectedDevice = null
            }
        }
    }

    private val scanReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                AndroidBluetoothDevice.ACTION_FOUND -> intent.getParcelableExtra<AndroidBluetoothDevice>(EXTRA_DEVICE)
                    ?.takeIf { it.name != null }
                    ?.let {
                        pairedAndroidDevices.add(it)
                        pairedDevicesFlow.update { pairedDevices -> pairedDevices + it.toBluetoothDevice() }
                    }

                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    logger.info("Bluetooth scanning finished")
                }
            }
        }
    }

    init {
        // 1. Initialize the HID Profile Proxy
        adapter.getProfileProxy(applicationContext, object : BluetoothProfile.ServiceListener {
            override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
                if (profile == BluetoothProfile.HID_DEVICE) {
                    hidDevice = proxy as BluetoothHidDevice
                    // 2. Register our Gamepad with the Bluetooth Stack
                    val executor = Executors.newSingleThreadExecutor()
                    hidDevice?.registerApp(serviceDiscoveryProtocolSettings, null, null, executor, callback)
                }
            }

            override fun onServiceDisconnected(profile: Int) {
                if (profile == BluetoothProfile.HID_DEVICE) hidDevice = null
            }
        }, BluetoothProfile.HID_DEVICE)

        // 3. Register Receiver for Scanning
        val filter = IntentFilter().apply {
            addAction(AndroidBluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
        applicationContext.registerReceiver(scanReceiver, filter)
    }

    // --- Scanning Logic ---

    override fun startScanning() {
        pairedAndroidDevices.clear()
        pairedDevicesFlow.update { emptySet() }
        if (adapter.isDiscovering) adapter.cancelDiscovery()
        adapter.startDiscovery()
    }


    // --- Connection Logic ---

    override fun connect(device: BluetoothDevice) {
        connectionStateFlow.update { ConnectionState.Connecting(device) }
        adapter.cancelDiscovery()
        // Most hosts require the device to be bonded first for HID
        val androidDevice = pairedAndroidDevices.find { it.address == device.mac }
        if (androidDevice!!.bondState == AndroidBluetoothDevice.BOND_NONE) {
            androidDevice.createBond()
        } else {
            hidDevice?.connect(androidDevice)
            connectionStateFlow.update { ConnectionState.Connected(device) }
        }
    }


    override fun send(controllerState: ControllerState) {
        // Total size is 13 bytes based on the alignment above
        val reportData = ByteArray(13)

        // Sticks (0-7)
        val lsX = mapStick16(controllerState.leftStickX)
        val lsY = mapStick16(controllerState.leftStickY)
        val rsX = mapStick16(controllerState.rightStickX)
        val rsY = mapStick16(controllerState.rightStickY)

        reportData[0] = (lsX and 0xFF).toByte()
        reportData[1] = ((lsX shr 8) and 0xFF).toByte()
        reportData[2] = (lsY and 0xFF).toByte()
        reportData[3] = ((lsY shr 8) and 0xFF).toByte()
        reportData[4] = (rsX and 0xFF).toByte()
        reportData[5] = ((rsX shr 8) and 0xFF).toByte()
        reportData[6] = (rsY and 0xFF).toByte()
        reportData[7] = ((rsY shr 8) and 0xFF).toByte()

        // Triggers (8-9)
        reportData[8] = (controllerState.l2.coerceIn(0f, 1f) * 255).toInt().toByte()
        reportData[9] = (controllerState.r2.coerceIn(0f, 1f) * 255).toInt().toByte()

        // Byte 10: Buttons 1-8
        var b1 = 0
        if (controllerState.a) b1 = b1 or (1 shl 0)
        if (controllerState.b) b1 = b1 or (1 shl 1)
        if (controllerState.x) b1 = b1 or (1 shl 2)
        if (controllerState.y) b1 = b1 or (1 shl 3)
        if (controllerState.l1) b1 = b1 or (1 shl 4)
        if (controllerState.r1) b1 = b1 or (1 shl 5)
        if (controllerState.select) b1 = b1 or (1 shl 6)
        if (controllerState.start) b1 = b1 or (1 shl 7)
        reportData[10] = b1.toByte()

        // Byte 11: Buttons 9-11
        var b2 = 0
        if (controllerState.l3) b2 = b2 or (1 shl 0)
        if (controllerState.r3) b2 = b2 or (1 shl 1)
        if (controllerState.guide) b2 = b2 or (1 shl 2)
        reportData[11] = b2.toByte()

        // Byte 12: Hat Switch (D-Pad)
        // Now it starts at the beginning of the byte (Bit 0)
        reportData[12] = calculateHat(controllerState.dpadX, controllerState.dpadY).toByte()

        hidDevice?.sendReport(connectedDevice, 0, reportData)
    }

    /**
     * Maps float -1.0..1.0 to 16-bit 0..65535
     */
    private fun mapStick16(value: Float): Int {
        return (((value.coerceIn(-1f, 1f) + 1f) / 2f) * 65535).toInt()
    }

    /**
     * Converts D-Pad floats to HID Hat Switch values (1-8, 0=Null)
     */
    private fun calculateHat(x: Float, y: Float): Int {
        val up = y < -0.5f
        val down = y > 0.5f
        val left = x < -0.5f
        val right = x > 0.5f

        return when {
            up && !left && !right -> 1    // North
            up && right -> 2              // North-East
            right && !up && !down -> 3    // East
            down && right -> 4            // South-East
            down && !left && !right -> 5  // South
            down && left -> 6             // South-West
            left && !up && !down -> 7     // West
            up && left -> 8               // North-West
            else -> 0                     // Center / Released
        }
    }

}