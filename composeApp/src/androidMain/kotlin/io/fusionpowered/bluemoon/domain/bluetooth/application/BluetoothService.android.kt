package io.fusionpowered.bluemoon.domain.bluetooth.application

import android.annotation.SuppressLint
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
import android.bluetooth.BluetoothProfile.HID_DEVICE
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import io.fusionpowered.bluemoon.domain.bluetooth.BluetoothConnectionProvider
import io.fusionpowered.bluemoon.domain.bluetooth.mapper.toBluetoothDevice
import io.fusionpowered.bluemoon.domain.bluetooth.mapper.toReport
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
    private val scannedAndroidDevices = mutableSetOf<AndroidBluetoothDevice>()
    private var hidDevice: BluetoothHidDevice? = null

    final override val savedDevicesFlow: StateFlow<Set<BluetoothDevice>>
        field = MutableStateFlow(adapter.bondedDevices.map { it.toBluetoothDevice() }.toSet())

    final override val scannedDevicesFlow: StateFlow<Set<BluetoothDevice>>
        field = MutableStateFlow(emptySet())

    final override val connectionStateFlow: StateFlow<ConnectionState>
        field = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)

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

    init {
        adapter.getProfileProxy(
            applicationContext,
            object : BluetoothProfile.ServiceListener {

                override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
                    if (profile == HID_DEVICE) {
                        hidDevice = proxy as BluetoothHidDevice
                    }
                }

                override fun onServiceDisconnected(profile: Int) {
                    if (profile == HID_DEVICE) hidDevice = null
                }

            },
            HID_DEVICE
        )
        applicationContext.registerReceiver(
            object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    when (intent.action) {
                        ACTION_FOUND -> intent.getParcelableExtra<AndroidBluetoothDevice>(EXTRA_DEVICE)
                            ?.takeIf { it.name != null }
                            ?.let {
                                scannedAndroidDevices.add(it)
                                scannedDevicesFlow.update { scannedDevices -> scannedDevices + it.toBluetoothDevice() }
                            }

                        ACTION_BOND_STATE_CHANGED -> {
                            val device = intent
                                .getParcelableExtra<AndroidBluetoothDevice>(EXTRA_DEVICE)
                                ?.toBluetoothDevice()
                                ?: return
                            val bondState = intent.getIntExtra(EXTRA_BOND_STATE, BOND_NONE)
                            if (bondState != BOND_BONDED) return
                            savedDevicesFlow.update { it + device }
                            when (val connectionState = connectionStateFlow.value) {
                                is ConnectionState.Connecting if connectionState.device.mac == device.mac -> this@BluetoothService.connect(device)
                                else -> { /* Do nothing */
                                }
                            }
                        }
                    }
                }
            },
            IntentFilter().apply {
                addAction(ACTION_FOUND)
                addAction(ACTION_BOND_STATE_CHANGED)
            }
        )
    }

    override fun startScanning() {
        scannedAndroidDevices.clear()
        scannedDevicesFlow.update { emptySet() }
        if (adapter.isDiscovering) adapter.cancelDiscovery()
        adapter.startDiscovery()
    }

    override fun connect(device: BluetoothDevice) {
        adapter.cancelDiscovery()
        connectionStateFlow.update { ConnectionState.Connecting(device) }
        val remoteDevice = adapter.getRemoteDevice(device.mac)
        when {
            hidDevice?.connectedDevices?.contains(remoteDevice) == true -> connectionStateFlow.update { ConnectionState.Connected(device) }

            remoteDevice.bondState == BOND_NONE -> remoteDevice.createBond()

            else -> {
                hidDevice?.registerApp(
                    serviceDiscoveryProtocolSettings,
                    null,
                    null,
                    Executors.newSingleThreadExecutor(),
                    object : BluetoothHidDevice.Callback() {

                        override fun onConnectionStateChanged(androidDevice: AndroidBluetoothDevice?, state: Int) {
                            val device = androidDevice
                                .takeIf { it != null }
                                ?.toBluetoothDevice()
                                ?: return

                            when (state) {
                                BluetoothProfile.STATE_CONNECTED -> {
                                    connectionStateFlow.update { ConnectionState.Connected(device) }
                                }

                                BluetoothProfile.STATE_DISCONNECTED -> {
                                    connectionStateFlow.update { ConnectionState.Disconnected }
                                }
                            }
                        }

                    }
                )
                if (hidDevice?.connect(remoteDevice) != true) {
                    connectionStateFlow.update { ConnectionState.Disconnected }
                }
            }
        }
    }

    override fun disconnect(device: BluetoothDevice) {
        hidDevice?.disconnect(adapter.getRemoteDevice(device.mac))
        hidDevice?.unregisterApp()
        connectionStateFlow.update { ConnectionState.Disconnected }
    }

    override fun send(device: BluetoothDevice, controllerState: ControllerState) {
        hidDevice?.sendReport(
            adapter.getRemoteDevice(device.mac),
            0,
            controllerState.toReport()
        )
    }

}