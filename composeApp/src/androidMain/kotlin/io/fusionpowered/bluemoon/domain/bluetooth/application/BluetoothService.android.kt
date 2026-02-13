package io.fusionpowered.bluemoon.domain.bluetooth.application

import android.annotation.SuppressLint
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
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import io.fusionpowered.bluemoon.domain.bluetooth.BluetoothClient
import io.fusionpowered.bluemoon.domain.bluetooth.mapper.toBluetoothDevice
import io.fusionpowered.bluemoon.domain.bluetooth.mapper.toReport
import io.fusionpowered.bluemoon.domain.bluetooth.model.BluemoonHid
import io.fusionpowered.bluemoon.domain.bluetooth.model.BluetoothDevice
import io.fusionpowered.bluemoon.domain.bluetooth.model.ConnectionState
import io.fusionpowered.bluemoon.domain.controller.model.ControllerState
import io.fusionpowered.bluemoon.domain.keyboard.model.KeyboardState
import io.fusionpowered.bluemoon.domain.touchpad.model.TouchpadState
import io.fusionpowered.bluemoon.domain.volume.model.VolumeState
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.Executors
import kotlin.Boolean
import kotlin.Int
import kotlin.Unit
import kotlin.apply
import kotlin.checkNotNull
import kotlin.collections.minus
import kotlin.collections.plus
import kotlin.experimental.or
import kotlin.getValue
import kotlin.let
import kotlin.takeIf

private typealias AndroidBluetoothDevice = android.bluetooth.BluetoothDevice

@SuppressLint("MissingPermission")
@Single
actual class BluetoothService actual constructor() : KoinComponent, BluetoothClient {

    private val applicationContext by inject<Context>()
    private val manager = applicationContext.getSystemService(BluetoothManager::class.java)
    private val adapter = checkNotNull(manager.adapter)
    private val lifecycle = ProcessLifecycleOwner.get().lifecycle

    final override val pairedDevices: StateFlow<Set<BluetoothDevice>>
        field = MutableStateFlow(
            adapter
                .bondedDevices
                .map { it.toBluetoothDevice() }
                .toSet()
        )

    final override val connectionStateFlow: StateFlow<ConnectionState>
        field = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)

    private var hidDevice: BluetoothHidDevice? = null
    private var lastConnectedDevice: BluetoothDevice? = null

    init {
        adapter.getProfileProxy(
            applicationContext,
            object : BluetoothProfile.ServiceListener {

                override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
                    if (profile == HID_DEVICE) {
                        hidDevice = proxy as BluetoothHidDevice

                        hidDevice?.connectedDevices
                            ?.firstOrNull()
                            ?.toBluetoothDevice()
                            ?.let { device ->
                                lastConnectedDevice = device
                                connectionStateFlow.update { ConnectionState.Connected(device) }
                            }
                    }
                }


                override fun onServiceDisconnected(profile: Int) {
                    if (profile == HID_DEVICE) hidDevice = null
                }

            },
            HID_DEVICE
        )
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                runBlocking {
                    lastConnectedDevice?.let { connect(it) }
                }
            }

            override fun onStop(owner: LifecycleOwner) {
                when (val it = connectionStateFlow.value) {
                    is ConnectionState.Connecting -> disconnect(it.device)
                    else -> {}
                }
            }
        })
        applicationContext.registerReceiver(
            object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    when (intent.action) {
                        ACTION_BOND_STATE_CHANGED -> {
                            val device = intent
                                .getParcelableExtra<AndroidBluetoothDevice>(EXTRA_DEVICE)
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
            },
            IntentFilter().apply {
                addAction(ACTION_BOND_STATE_CHANGED)
                addAction(ACTION_ACL_DISCONNECTED)
                addAction(ACTION_STATE_CHANGED)
            }
        )
    }

    override suspend fun connect(device: BluetoothDevice) {
        connectionStateFlow.update { ConnectionState.Connecting(device) }

        val remoteDevice = adapter.getRemoteDevice(device.mac)
        when {
            hidDevice?.connectedDevices?.contains(remoteDevice) == true -> connectionStateFlow.update { ConnectionState.Connected(device) }

            else -> {
                ensureAppRegistration()
                if (hidDevice?.connect(remoteDevice) != true) {
                    connectionStateFlow.update { ConnectionState.Disconnected }
                }
            }
        }
    }

    suspend fun ensureAppRegistration() {
        val registrationComplete = CompletableDeferred<Unit>()
        hidDevice?.unregisterApp()
        hidDevice?.registerApp(
            BluetoothHidDeviceAppSdpSettings(
                BluemoonHid.NAME,
                BluemoonHid.DESCRIPTION,
                BluemoonHid.MANUFACTURER,
                SUBCLASS1_COMBO or SUBCLASS1_COMBO,
                BluemoonHid.DESCRIPTOR.toByteArray(),
            ),
            null,
            null,
            Executors.newSingleThreadExecutor(),
            object : BluetoothHidDevice.Callback() {

                override fun onAppStatusChanged(pluggedDevice: android.bluetooth.BluetoothDevice?, registered: Boolean) {
                    super.onAppStatusChanged(pluggedDevice, registered)
                    if (registered) {
                        registrationComplete.complete(Unit)
                    }
                }

                override fun onConnectionStateChanged(androidDevice: AndroidBluetoothDevice?, state: Int) {
                    val device = androidDevice
                        .takeIf { it != null }
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
        )
        registrationComplete.await()
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