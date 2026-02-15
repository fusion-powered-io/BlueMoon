package io.fusionpowered.bluemoon.presentation

import android.Manifest.permission.BLUETOOTH_ADVERTISE
import android.Manifest.permission.BLUETOOTH_CONNECT
import android.Manifest.permission.BLUETOOTH_SCAN
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.S
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
import android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.SideEffect
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import io.fusionpowered.bluemoon.domain.controller.application.ControllerService
import io.fusionpowered.bluemoon.domain.volume.application.VolumeService
import org.koin.android.ext.android.inject

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
                permissionsState.allPermissionsGranted -> UiEntryPoint()
                else -> SideEffect { permissionsState.launchMultiplePermissionRequest() }
            }
        }
    }

    private fun setFullscreen() {
        // Force the layout to use the entire screen including under system bars
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Use the Compat controller for stable behavior across Android versions
        val controller = WindowInsetsControllerCompat(window, window.decorView)

        // Hide both status bars (top) and navigation bars (bottom)
        controller.hide(WindowInsetsCompat.Type.systemBars())

        // This allows the bars to float over the app then disappear automatically
        controller.systemBarsBehavior = BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        // Handle the Notch/Camera Cutout
        window.attributes.layoutInDisplayCutoutMode = LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
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