package io.fusionpowered.bluemoon.domain.bluetooth.adapter.foregroundservice

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.Service
import android.bluetooth.BluetoothAdapter.ACTION_STATE_CHANGED
import android.bluetooth.BluetoothAdapter.EXTRA_STATE
import android.bluetooth.BluetoothAdapter.STATE_OFF
import android.bluetooth.BluetoothDevice.ACTION_ACL_DISCONNECTED
import android.bluetooth.BluetoothDevice.ACTION_BOND_STATE_CHANGED
import android.bluetooth.BluetoothDevice.BOND_BONDED
import android.bluetooth.BluetoothDevice.BOND_NONE
import android.bluetooth.BluetoothDevice.EXTRA_BOND_STATE
import android.bluetooth.BluetoothDevice.EXTRA_DEVICE
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothHidDevice.SUBCLASS1_COMBO
import android.bluetooth.BluetoothHidDeviceAppSdpSettings
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothProfile.HID_DEVICE
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
import android.os.Binder
import android.os.Build
import android.os.IBinder
import io.fusionpowered.bluemoon.R
import io.fusionpowered.bluemoon.domain.bluetooth.mapper.toBluetoothDevice
import io.fusionpowered.bluemoon.domain.bluetooth.mapper.toReport
import io.fusionpowered.bluemoon.domain.bluetooth.model.BluemoonHid
import io.fusionpowered.bluemoon.domain.bluetooth.model.BluetoothDevice
import io.fusionpowered.bluemoon.domain.bluetooth.model.ConnectionState
import io.fusionpowered.bluemoon.domain.bluetooth.port.BluetoothClient
import io.fusionpowered.bluemoon.domain.controller.model.ControllerState
import io.fusionpowered.bluemoon.domain.keyboard.model.KeyboardState
import io.fusionpowered.bluemoon.domain.touchpad.model.TouchpadState
import io.fusionpowered.bluemoon.domain.volume.model.VolumeState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.Executors.newSingleThreadExecutor
import kotlin.experimental.or

private typealias AndroidBluetoothDevice = android.bluetooth.BluetoothDevice

@SuppressLint("MissingPermission")
class ForegroundServiceBluetoothClient : Service(), BluetoothClient {

    private val manager by lazy { getSystemService(BluetoothManager::class.java)!! }
    private val adapter by lazy { manager.adapter }

    final override val pairedDevices = MutableStateFlow<Set<BluetoothDevice>>(emptySet())

    final override val connectionStateFlow = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)

    private var hidDevice: BluetoothHidDevice? = null
    private var lastConnectedDevice: BluetoothDevice? = null

    val localBinder = LocalBinder()

    inner class LocalBinder : Binder() {

        fun getService(): ForegroundServiceBluetoothClient = this@ForegroundServiceBluetoothClient
    }

    override fun onCreate() {
        super.onCreate()
        pairedDevices.update {
            adapter.bondedDevices.map { it.toBluetoothDevice() }.toSet()
        }
        startAsForeground()
        registerProfileProxyListeners()
        registerBroadcastReceiver()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private fun startAsForeground() {
        val channelId = "bluemoon_hid_channel"

        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(NotificationChannel(channelId, "BlueMoon", IMPORTANCE_LOW))

        val notification = Notification.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle("BlueMoon controller active")
            .setContentText("Your device is available as a Bluetooth controller")
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(1, notification, FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE)
        } else {
            startForeground(1, notification)
        }
    }

    private fun registerProfileProxyListeners() {
        val bluetoothServiceListener = object : BluetoothProfile.ServiceListener {
            override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
                if (profile == HID_DEVICE) {
                    hidDevice = (proxy as BluetoothHidDevice)
                        .also { registerHidCallbacks(it) }
                        .also {
                            it.connectedDevices
                                .firstOrNull()
                                ?.toBluetoothDevice()
                                ?.let { device ->
                                    lastConnectedDevice = device
                                    connectionStateFlow.update { ConnectionState.Connected(device) }
                                }
                        }
                }
            }

            override fun onServiceDisconnected(profile: Int) {
                if (profile == HID_DEVICE) hidDevice = null
            }
        }
        adapter.getProfileProxy(applicationContext, bluetoothServiceListener, HID_DEVICE)
    }

    private fun registerHidCallbacks(hidDevice: BluetoothHidDevice) {
        val sdpSettings = BluetoothHidDeviceAppSdpSettings(
            BluemoonHid.NAME,
            BluemoonHid.DESCRIPTION,
            BluemoonHid.MANUFACTURER,
            SUBCLASS1_COMBO or SUBCLASS1_COMBO,
            BluemoonHid.DESCRIPTOR.toByteArray(),
        )
        val hidCallbacks = object : BluetoothHidDevice.Callback() {
            override fun onConnectionStateChanged(androidDevice: AndroidBluetoothDevice?, state: Int) {
                val device = androidDevice
                    .takeIf { it != null }
                    ?.takeIf { it.name != null }
                    ?.toBluetoothDevice()
                    ?: return

                when (state) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        lastConnectedDevice = device
                        connectionStateFlow.update { ConnectionState.Connected(device) }
                    }

                    BluetoothProfile.STATE_DISCONNECTED -> {
                        connectionStateFlow.update { ConnectionState.Disconnected }
                    }
                }
            }
        }
        hidDevice.registerApp(sdpSettings, null, null, newSingleThreadExecutor(), hidCallbacks)
    }

    private fun registerBroadcastReceiver() {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when (intent.action) {
                    ACTION_BOND_STATE_CHANGED -> {
                        val device = intent
                            .getParcelableExtra<AndroidBluetoothDevice>(
                                EXTRA_DEVICE
                            )
                            ?.toBluetoothDevice()
                            ?: return
                        when (intent.getIntExtra(EXTRA_BOND_STATE, BOND_NONE)) {
                            BOND_BONDED -> pairedDevices.update { it + device }
                            BOND_NONE -> pairedDevices.update { it - device }
                            else -> {}
                        }
                    }

                    ACTION_ACL_DISCONNECTED -> {
                        val device = intent
                            .getParcelableExtra<AndroidBluetoothDevice>(EXTRA_DEVICE)
                            ?.toBluetoothDevice()
                            ?: return

                        when (val connectionState = connectionStateFlow.value) {
                            is ConnectionState.Connected if connectionState.device == device -> {
                                lastConnectedDevice = null
                                connectionStateFlow.update { ConnectionState.Disconnected }
                            }

                            else -> {}
                        }
                    }

                    ACTION_STATE_CHANGED -> {
                        if (intent.getIntExtra(EXTRA_STATE, STATE_OFF) == STATE_OFF) {
                            lastConnectedDevice = null
                            connectionStateFlow.update { ConnectionState.Disconnected }
                        }
                    }
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(ACTION_BOND_STATE_CHANGED)
            addAction(ACTION_ACL_DISCONNECTED)
            addAction(ACTION_STATE_CHANGED)
        }
        registerReceiver(receiver, filter)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder = localBinder

    override suspend fun connect(device: BluetoothDevice) {
        connectionStateFlow.update { ConnectionState.Connecting(device) }

        val remoteDevice = adapter.getRemoteDevice(device.mac)
        when {
            hidDevice?.connectedDevices?.contains(remoteDevice) == true -> connectionStateFlow.update {
                ConnectionState.Connected(
                    device
                )
            }

            else -> {
                if (hidDevice?.connect(remoteDevice) != true) {
                    connectionStateFlow.update { ConnectionState.Disconnected }
                }
            }
        }
    }

    override fun disconnect(device: BluetoothDevice) {
        lastConnectedDevice = null
        hidDevice?.disconnect(adapter.getRemoteDevice(device.mac))
        connectionStateFlow.update { ConnectionState.Disconnected }
    }

    override fun send(device: BluetoothDevice, controllerState: ControllerState) {
        val androidDevice = adapter.getRemoteDevice(device.mac)
        hidDevice?.sendReport(
            androidDevice,
            1,
            controllerState.toReport()
        )
    }

    override fun send(device: BluetoothDevice, keyboardState: KeyboardState) {
        val androidDevice = adapter.getRemoteDevice(device.mac)
        hidDevice?.sendReport(
            androidDevice,
            2,
            keyboardState.toReport()
        )
    }

    override fun send(device: BluetoothDevice, touchPadState: TouchpadState) {
        val androidDevice = adapter.getRemoteDevice(device.mac)
        hidDevice?.sendReport(
            androidDevice,
            3,
            touchPadState.toReport()
        )
    }

    override fun send(device: BluetoothDevice, volumeState: VolumeState) {
        val androidDevice = adapter.getRemoteDevice(device.mac)
        hidDevice?.sendReport(
            androidDevice,
            4,
            volumeState.toReport()
        )
    }
}
