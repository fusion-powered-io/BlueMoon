package io.fusionpowered.bluemoon.presentation

import android.Manifest.permission.BLUETOOTH_ADVERTISE
import android.Manifest.permission.BLUETOOTH_CONNECT
import android.Manifest.permission.BLUETOOTH_SCAN
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.S
import android.os.Bundle
import android.os.IBinder
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import io.fusionpowered.bluemoon.domain.bluetooth.adapter.foregroundservice.ForegroundServiceBluetoothClient
import io.fusionpowered.bluemoon.domain.bluetooth.port.BluetoothClient
import io.fusionpowered.bluemoon.domain.controller.application.ControllerService
import io.fusionpowered.bluemoon.domain.volume.application.VolumeService
import kotlinx.coroutines.CompletableDeferred
import org.koin.android.ext.android.inject
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

class MainActivity : ComponentActivity() {

    val inputReceiver by inject<ControllerService>()
    val volumeReceiver by inject<VolumeService>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFullscreen()
        window.addFlags(FLAG_KEEP_SCREEN_ON)
        val requiredPermissions = if (SDK_INT >= S) listOf(BLUETOOTH_SCAN, BLUETOOTH_CONNECT, BLUETOOTH_ADVERTISE) else emptyList()
        setContent {
            val permissionsState = rememberMultiplePermissionsState(requiredPermissions)
            when {
                permissionsState.allPermissionsGranted -> {
                    var serviceReady by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        serviceReady = ensureForegroundServiceReady()
                    }
                    if (serviceReady) {
                        UiEntryPoint()
                    }
                }

                else -> SideEffect {
                    permissionsState.launchMultiplePermissionRequest()
                }
            }
        }
    }

    private fun setFullscreen() {
        enableEdgeToEdge()
        WindowInsetsControllerCompat(window, window.decorView)
            .apply { systemBarsBehavior = BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE }
            .hide(WindowInsetsCompat.Type.systemBars())
    }

    private suspend fun ensureForegroundServiceReady(): Boolean {
        val serviceReady = CompletableDeferred<Boolean>()
        val connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                loadKoinModules(module {
                    single<BluetoothClient> { (binder as ForegroundServiceBluetoothClient.LocalBinder).getService() }
                })
                serviceReady.complete(true)
            }

            override fun onServiceDisconnected(name: ComponentName?) {}
        }
        val intent = Intent(this, ForegroundServiceBluetoothClient::class.java)
        bindService(intent, connection, BIND_AUTO_CREATE)
        startForegroundService(Intent(this, ForegroundServiceBluetoothClient::class.java))

        return try {
            serviceReady.await()
        } finally {
            unbindService(connection)
        }
    }

    override fun onGenericMotionEvent(event: MotionEvent?): Boolean =
        when {
            event == null -> true

            else -> {
                inputReceiver.handle(event)
                true
            }
        }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean =
        when {
            event == null -> true

            event.isFromController() -> {
                inputReceiver.handle(event)
                true
            }

            event.isVolumeButton() -> {
                volumeReceiver.handle(event)
                true
            }

            else -> false
        }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean =
        when {
            event == null -> true

            event.isFromController() -> {
                inputReceiver.handle(event)
                true
            }

            event.isVolumeButton() -> {
                volumeReceiver.handle(event)
                true
            }

            else -> false
        }

    override fun onKeyMultiple(keyCode: Int, repeatCount: Int, event: KeyEvent?): Boolean =
        when {
            event == null -> true

            event.isFromController() -> {
                inputReceiver.handle(event)
                true
            }

            else -> false
        }

    private fun KeyEvent.isFromController() =
        KeyEvent.isGamepadButton(keyCode) || keyCode == KeyEvent.KEYCODE_BACK

    private fun KeyEvent.isVolumeButton() =
        keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN

}