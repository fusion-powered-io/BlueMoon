package io.fusionpowered.bluemoon.domain.bluetooth.application

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter.ACTION_DISCOVERY_FINISHED
import android.bluetooth.BluetoothDevice.ACTION_BOND_STATE_CHANGED
import android.bluetooth.BluetoothDevice.ACTION_FOUND
import android.bluetooth.BluetoothDevice.BOND_BONDED
import android.bluetooth.BluetoothDevice.BOND_NONE
import android.bluetooth.BluetoothDevice.EXTRA_BOND_STATE
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

    var scannedAndroidDevices = mutableSetOf<AndroidBluetoothDevice>()

    final override val savedDevicesFlow: StateFlow<Set<BluetoothDevice>>
        field = MutableStateFlow(adapter.bondedDevices.map { it.toBluetoothDevice() }.toSet())

    final override val scannedDevicesFlow: StateFlow<Set<BluetoothDevice>>
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
            0x05u, 0x01u,        // Usage Page (Generic Desktop)
            0x09u, 0x05u,        // Usage (Game Pad)
            0xA1u, 0x01u,        // Collection (Application)

            // Sticks: X, Y, Z, Rz (4 x 16-bit)
            0x05u, 0x01u,        //   Usage Page (Generic Desktop)
            0x09u, 0x30u,        //   Usage (X)
            0x09u, 0x31u,        //   Usage (Y)
            0x09u, 0x32u,        //   Usage (Z) - often Right Stick X
            0x09u, 0x35u,        //   Usage (Rz) - often Right Stick Y
            0x15u, 0x00u,        //   Logical Minimum (0)
            0x27u, 0xFFu, 0xFFu, 0x00u, 0x00u, // Logical Maximum (65535)
            0x75u, 0x10u,        //   Report Size (16 bits)
            0x95u, 0x04u,        //   Report Count (4 axes)
            0x81u, 0x02u,        //   Input (Data, Variable, Absolute)

            // Buttons: 12 Buttons (12 bits)
            0x05u, 0x09u,        //   Usage Page (Button)
            0x19u, 0x01u,        //   Usage Minimum (Button 1)
            0x29u, 0x0Cu,        //   Usage Maximum (Button 12)
            0x15u, 0x00u,        //   Logical Minimum (0)
            0x25u, 0x01u,        //   Logical Maximum (1)
            0x75u, 0x01u,        //   Report Size (1)
            0x95u, 0x0Cu,        //   Report Count (12)
            0x81u, 0x02u,        //   Input (Data, Variable, Absolute)

            // Padding: 4 bits to finish the 2nd button byte
            0x75u, 0x01u,        //   Report Size (1)
            0x95u, 0x04u,        //   Report Count (4)
            0x81u, 0x03u,        //   Input (Constant, Variable, Absolute)

            // Hat Switch (4 bits)
            0x05u, 0x01u,        //   Usage Page (Generic Desktop)
            0x09u, 0x39u,        //   Usage (Hat Switch)
            0x15u, 0x01u,        //   Logical Minimum (1)
            0x25u, 0x08u,        //   Logical Maximum (8)
            0x35u, 0x00u,        //   Physical Minimum (0)
            0x46u, 0x3Bu, 0x01u, //   Physical Maximum (315) - for degrees
            0x66u, 0x14u, 0x00u, //   Unit (English Rotation: Degrees)
            0x75u, 0x04u,        //   Report Size (4 bits)
            0x95u, 0x01u,        //   Report Count (1)
            0x81u, 0x42u,        //   Input (Data, Variable, Absolute, Null State)

            // Final Padding: 4 bits to finish the final byte
            0x75u, 0x04u,        //   Report Size (4)
            0x95u, 0x01u,        //   Report Count (1)
            0x81u, 0x03u,        //   Input (Constant, Variable, Absolute)

            0xC0u                // End Collection
        ).toByteArray()
    )

    private val callback = object : BluetoothHidDevice.Callback() {
        override fun onConnectionStateChanged(androidDevice: AndroidBluetoothDevice?, state: Int) {
            val device = androidDevice
                .takeIf { it != null }
                ?.toBluetoothDevice()
                ?: return

            when (state) {
                BluetoothProfile.STATE_CONNECTED -> {
                    connectedDevice = androidDevice
                    connectionStateFlow.update { ConnectionState.Connected(device) }
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    connectedDevice = null
                    connectionStateFlow.update { ConnectionState.Disconnected }
                }
            }
        }
    }

    private val scanReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_FOUND -> intent.getParcelableExtra<AndroidBluetoothDevice>(EXTRA_DEVICE)
                    ?.takeIf { it.name != null }
                    ?.let {
                        scannedAndroidDevices.add(it)
                        scannedDevicesFlow.update { scannedDevices -> scannedDevices + it.toBluetoothDevice() }
                    }

                ACTION_DISCOVERY_FINISHED -> {
                    logger.info("Bluetooth scanning finished")
                }

                ACTION_BOND_STATE_CHANGED -> {
                    val device = intent.getParcelableExtra<AndroidBluetoothDevice>(EXTRA_DEVICE) ?: return
                    val bondState = intent.getIntExtra(EXTRA_BOND_STATE, BOND_NONE)
                    if (bondState != BOND_BONDED) return
                    savedDevicesFlow.update { it + device.toBluetoothDevice() }
                    when (val connectionState = connectionStateFlow.value) {
                        is ConnectionState.Connecting if connectionState.device.mac == device.address -> hidDevice?.connect(device)
                        else -> { /* Do nothing */
                        }
                    }
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
            addAction(ACTION_FOUND)
            addAction(ACTION_DISCOVERY_FINISHED)
        }
        applicationContext.registerReceiver(scanReceiver, filter)
    }

    // --- Scanning Logic ---

    override fun startScanning() {
        scannedAndroidDevices.clear()
        scannedDevicesFlow.update { emptySet() }
        if (adapter.isDiscovering) adapter.cancelDiscovery()
        adapter.startDiscovery()
    }


    // --- Connection Logic ---

    override fun connect(device: BluetoothDevice) {
        adapter.cancelDiscovery()
        connectionStateFlow.update { ConnectionState.Connecting(device) }
        scannedAndroidDevices.find { it.address == device.mac }
            ?.let { androidDevice ->
                if (androidDevice.bondState == BOND_NONE) {
                    androidDevice.createBond()
                } else {
                    hidDevice?.connect(androidDevice)
                }
            }

    }

    override fun disconnect() {
        if (connectedDevice == null) {
            connectionStateFlow.update { ConnectionState.Disconnected }
        }

        hidDevice?.disconnect(connectedDevice)

        connectedDevice = null
    }

    override fun send(controllerState: ControllerState) {
        // Total size is 12 bytes based on the aligned descriptor:
        // Sticks (8) + Buttons (2) + Hat/Padding (1) + Extra Padding (1)
        val reportData = ByteArray(12)

        // Sticks (Bytes 0-7): X, Y, Z, Rz (4 x 16-bit)
        // Most games map L-Stick to X/Y and R-Stick to Z/Rz
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

        // Byte 8: Buttons 1-8
        var b1 = 0
        if (controllerState.a) b1 = b1 or (1 shl 0)
        if (controllerState.b) b1 = b1 or (1 shl 1)
        //if (controllerState.x) b1 = b1 or (1 shl 2) Doesn't seem to be used
        if (controllerState.x) b1 = b1 or (1 shl 3)
        if (controllerState.y) b1 = b1 or (1 shl 4)
        // if (controllerState.r1) b1 = b1 or (1 shl 5) Doesn't seem to be used
        if (controllerState.l1) b1 = b1 or (1 shl 6)
        if (controllerState.r1) b1 = b1 or (1 shl 7)
        reportData[8] = b1.toByte()

        // Byte 9: Buttons 9-12
        var b2 = 0
        // if (controllerState.select) b2 = b2 or (1 shl 0) Doesn't seem to be used
        // if (controllerState.start) b2 = b2 or (1 shl 1) Doesn't seem to be used
        if (controllerState.select) b2 = b2 or (1 shl 2)
        if (controllerState.start) b2 = b2 or (1 shl 3)
        // Recommended slots for the remaining controls:
        if (controllerState.r3)     b2 = b2 or (1 shl 5) // Bit 5
        if (controllerState.guide)  b2 = b2 or (1 shl 6) // Bit 6
        if (controllerState.l3)     b2 = b2 or (1 shl 7) // Bit 4
        // Bits 4-7 are padding (Constant)
        reportData[9] = b2.toByte()

        // Byte 10: Hat Switch (Bits 0-3) + Padding (Bits 4-7)
        val hatValue = calculateHat(controllerState.dpadX, controllerState.dpadY)
        // Ensure hat is only 4 bits and doesn't bleed into padding
        reportData[10] = (hatValue and 0x0F).toByte()

        // Byte 11: Extra Padding byte
        // Required to ensure the report matches the descriptor's expected length
        reportData[11] = 0x00.toByte()

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