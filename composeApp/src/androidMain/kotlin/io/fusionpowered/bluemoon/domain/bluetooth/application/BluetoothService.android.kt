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
        BluetoothHidDevice.SUBCLASS2_GAMEPAD,
        ubyteArrayOf(
            0x05u, 0x01u,                       // Usage Page (Generic Desktop)
            0x09u, 0x05u,                       // Usage (Game Pad)
            0xA1u, 0x01u,                       // Collection (Application)

            // Sticks: X, Y, Z, Rz (4 x 16-bit)
            0x05u, 0x01u,                       //   Usage Page (Generic Desktop)
            0x09u, 0x30u,                       //   Usage (X)
            0x09u, 0x31u,                       //   Usage (Y)
            0x09u, 0x32u,                       //   Usage (Z) - often Right Stick X
            0x09u, 0x35u,                       //   Usage (Rz) - often Right Stick Y
            0x15u, 0x00u,                       //   Logical Minimum (0)
            0x27u, 0xFFu, 0xFFu, 0x00u, 0x00u,  // Logical Maximum (65535)
            0x75u, 0x10u,                       //   Report Size (16 bits)
            0x95u, 0x04u,                       //   Report Count (4 axes)
            0x81u, 0x02u,                       //   Input (Data, Variable, Absolute)*/

            // Triggers
            0x05u, 0x02u,                       //     USAGE_PAGE (Simulation Control)
            0x15u, 0x00u,                       //     LOGICAL_MINIMUM (0)
            0x26u, 0xFFu, 0x00u,                //     LOGICAL_MAXIMUM (255)
            0x09u, 0xC4u,                       //     USAGE(Acceleration)
            0x09u, 0xC5u,                       //     USAGE(Brake)
            0x75u, 0x08u,                       //     REPORT_SIZE (8)
            0x95u, 0x02u,                       //     REPORT_COUNT (2)
            0x81u, 0x02u,                       //     INPUT (Data,Var,Abs)

            // Buttons: 16 Buttons (16 bits)
            0x05u, 0x09u,                       //   Usage Page (Button)
            0x19u, 0x01u,                       //   Usage Minimum (Button 1)
            0x29u, 0x10u,                       //   Usage Maximum (Button 16)
            0x15u, 0x00u,                       //   Logical Minimum (0)
            0x25u, 0x01u,                       //   Logical Maximum (1)
            0x95u, 0x10u,                       //   Report Count (16)
            0x75u, 0x01u,                       //   Report Size (1)
            0x81u, 0x02u,                       //   Input (Data, Variable, Absolute)

            // Hat Switch (4 bits)
            0x05u, 0x01u,                       //   Usage Page (Generic Desktop)
            0x09u, 0x39u,                       //   Usage (Hat Switch)
            0x15u, 0x01u,                       //   Logical Minimum (1)
            0x25u, 0x08u,                       //   Logical Maximum (8)
            0x35u, 0x00u,                       //   Physical Minimum (0)
            0x46u, 0x3Bu, 0x01u,                //   Physical Maximum (315) - for degrees
            0x66u, 0x14u, 0x00u,                //   Unit (English Rotation: Degrees)
            0x75u, 0x04u,                       //   Report Size (4 bits)
            0x95u, 0x01u,                       //   Report Count (1)
            0x81u, 0x42u,                       //   Input (Data, Variable, Absolute, Null State)

            // Final Padding: 4 bits to finish the final byte
            0x75u, 0x04u,                       //   Report Size (4)
            0x95u, 0x01u,                       //   Report Count (1)
            0x81u, 0x03u,                       //   Input (Constant, Variable, Absolute)

            0xC0u                               // End Collection
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
        // stickBytes(8) + triggerBytes(2) + buttonBytes(2) + hatBytes(1) + paddingBytes(1)
        val reportData = ByteArray(14)

        controllerState.leftStickX.to16bit().let {
            reportData[0] = (it and 0xFF).toByte()
            reportData[1] = ((it shr 8) and 0xFF).toByte()
        }
        controllerState.leftStickY.to16bit().let {
            reportData[2] = (it and 0xFF).toByte()
            reportData[3] = ((it shr 8) and 0xFF).toByte()
        }
        controllerState.rightStickX.to16bit().let {
            reportData[4] = (it and 0xFF).toByte()
            reportData[5] = ((it shr 8) and 0xFF).toByte()
        }
        controllerState.rightStickY.to16bit().let {
            reportData[6] = (it and 0xFF).toByte()
            reportData[7] = ((it shr 8) and 0xFF).toByte()
        }

        reportData[8] = controllerState.r2.to8bit().toByte()
        reportData[9] = controllerState.l2.to8bit().toByte()

        reportData[10] = controllerState.run {
            var buttons = 0
            if (a) buttons = buttons or (1 shl 0)
            if (b) buttons = buttons or (1 shl 1)
            if (x) buttons = buttons or (1 shl 3)
            if (y) buttons = buttons or (1 shl 4)
            if (l1) buttons = buttons or (1 shl 6)
            if (r1) buttons = buttons or (1 shl 7)
            buttons.toByte()
        }
        reportData[11] = controllerState.run {
            var buttons = 0
            if (l3) buttons = buttons or (1 shl 0)
            if (l3) buttons = buttons or (1 shl 1)
            if (select) buttons = buttons or (1 shl 2)
            if (start) buttons = buttons or (1 shl 3)
            if (guide) buttons = buttons or (1 shl 4)
            buttons.toByte()
        }

        reportData[12] = controllerState.run {
            val up = dpadY < -0.5f
            val down = dpadY > 0.5f
            val left = dpadX < -0.5f
            val right = dpadX > 0.5f
            val hat = when {
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
            (hat and 0x0F).toByte()
        }

        //Extra Padding byte
        reportData[13] = 0x00.toByte()

        hidDevice?.sendReport(connectedDevice, 0, reportData)
    }

    private fun Float.to16bit(): Int =
        (((coerceIn(-1f, 1f) + 1f) / 2f) * 65535).toInt()

    private fun Float.to8bit(): Int =
        (coerceIn(0f, 1f) * 255).toInt()

}