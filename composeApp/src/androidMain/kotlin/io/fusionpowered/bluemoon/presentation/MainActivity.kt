package io.fusionpowered.bluemoon.presentation

import android.Manifest.permission.BLUETOOTH_ADVERTISE
import android.Manifest.permission.BLUETOOTH_CONNECT
import android.Manifest.permission.BLUETOOTH_SCAN
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.S
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.Window.FEATURE_NO_TITLE
import android.view.WindowInsets.Type.navigationBars
import android.view.WindowInsets.Type.statusBars
import android.view.WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.SideEffect
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
        enableEdgeToEdge()
        requestWindowFeature(FEATURE_NO_TITLE)
        window.insetsController?.hide(statusBars() or navigationBars())
        window.addFlags(FLAG_LAYOUT_IN_SCREEN)
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