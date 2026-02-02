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
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.SideEffect
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import io.fusionpowered.bluemoon.domain.controller.application.ControllerService
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    val inputReceiver by inject<ControllerService>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFullscreen()
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
        // 1. Force the layout to use the entire screen including under system bars
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // 2. Use the Compat controller for stable behavior across Android versions
        val controller = WindowInsetsControllerCompat(window, window.decorView)

        // Hide both status bars (top) and navigation bars (bottom)
        controller.hide(WindowInsetsCompat.Type.systemBars())

        // 3. THE FIX: Set behavior to "Transient" so swipes don't resize the UI
        // This allows the bars to float over the app then disappear automatically
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        // 4. Handle the Notch/Camera Cutout (Crucial for horizontal touchpads)
        window.attributes.layoutInDisplayCutoutMode =
            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
    }

    override fun onGenericMotionEvent(event: MotionEvent?): Boolean {
        if (event == null) {
            return true
        }
        inputReceiver.handle(event)
        return true
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (event == null) {
            return true
        }
        if (!event.isFromController()) {
            return false
        }
        inputReceiver.handle(event)
        return true
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (event == null) {
            return true
        }
        if (!event.isFromController()) {
            return false
        }
        inputReceiver.handle(event)
        return true
    }

    override fun onKeyMultiple(keyCode: Int, repeatCount: Int, event: KeyEvent?): Boolean {
        if (event == null) {
            return true
        }
        if (!event.isFromController()) {
            return false
        }
        inputReceiver.handle(event)
        return true
    }

    private fun KeyEvent.isFromController() =
        KeyEvent.isGamepadButton(keyCode) || keyCode == KeyEvent.KEYCODE_BACK


}