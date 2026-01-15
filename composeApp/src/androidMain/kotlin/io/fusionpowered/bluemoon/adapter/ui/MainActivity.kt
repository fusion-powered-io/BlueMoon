package io.fusionpowered.bluemoon.adapter.ui

import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.fusionpowered.bluemoon.ControllerInputHandler
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    val controllerInputHandler: ControllerInputHandler by inject()

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

        setContent {
            App()
        }
    }

    override fun onGenericMotionEvent(event: MotionEvent?): Boolean {
        if (event == null) {
            return true
        }
        controllerInputHandler.handle("(${event.rawX}, ${event.rawY})")
        return true
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (event == null) {
            return true
        }
        controllerInputHandler.handle(event.keyCode.toString())
        return true
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (event == null) {
            return true
        }
        controllerInputHandler.handle(event.keyCode.toString())
        return true
    }

    override fun onKeyMultiple(keyCode: Int, repeatCount: Int, event: KeyEvent?): Boolean {
        if (event == null) {
            return true
        }
        controllerInputHandler.handle(event.keyCode.toString())
        return true
    }

}