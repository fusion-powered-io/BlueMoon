package io.fusionpowered.bluemoon.presentation

import android.Manifest.permission.BLUETOOTH_ADVERTISE
import android.Manifest.permission.BLUETOOTH_CONNECT
import android.Manifest.permission.BLUETOOTH_SCAN
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import io.fusionpowered.bluemoon.domain.controller.application.ControllerService
import org.koin.android.ext.android.inject
import org.koin.core.logger.Logger

class MainActivity : ComponentActivity() {

    val logger by inject<Logger>()
    val inputReceiver by inject<ControllerService>()

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {

        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        // We don't want a title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        // Full-screen
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        // If we're going to use immersive mode, we want to have
        // the entire screen
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        // Listen for UI visibility events
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)

        if (checkSelfPermission(BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(BLUETOOTH_SCAN), 0)
        }

        if (checkSelfPermission(BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(BLUETOOTH_CONNECT), 0)
        }

        if (checkSelfPermission(BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(BLUETOOTH_ADVERTISE), 0)
        }

        setContent {
            UiEntryPoint()
        }
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