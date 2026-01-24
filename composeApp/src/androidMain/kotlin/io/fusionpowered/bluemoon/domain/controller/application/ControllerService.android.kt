package io.fusionpowered.bluemoon.domain.controller.application

import android.view.InputEvent
import android.view.KeyEvent
import android.view.KeyEvent.ACTION_DOWN
import android.view.KeyEvent.KEYCODE_BACK
import android.view.KeyEvent.KEYCODE_BUTTON_A
import android.view.KeyEvent.KEYCODE_BUTTON_B
import android.view.KeyEvent.KEYCODE_BUTTON_L1
import android.view.KeyEvent.KEYCODE_BUTTON_R1
import android.view.KeyEvent.KEYCODE_BUTTON_SELECT
import android.view.KeyEvent.KEYCODE_BUTTON_START
import android.view.KeyEvent.KEYCODE_BUTTON_THUMBL
import android.view.KeyEvent.KEYCODE_BUTTON_THUMBR
import android.view.KeyEvent.KEYCODE_BUTTON_X
import android.view.KeyEvent.KEYCODE_BUTTON_Y
import android.view.MotionEvent
import android.view.MotionEvent.AXIS_BRAKE
import android.view.MotionEvent.AXIS_GAS
import android.view.MotionEvent.AXIS_HAT_X
import android.view.MotionEvent.AXIS_HAT_Y
import android.view.MotionEvent.AXIS_RZ
import android.view.MotionEvent.AXIS_X
import android.view.MotionEvent.AXIS_Y
import android.view.MotionEvent.AXIS_Z
import io.fusionpowered.bluemoon.domain.controller.ControllerStateProvider
import io.fusionpowered.bluemoon.domain.controller.model.ControllerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent

@Single
actual class ControllerService actual constructor() : ControllerStateProvider, KoinComponent {

    final override val controllerStateFlow: StateFlow<ControllerState>
        field = MutableStateFlow<ControllerState>(ControllerState())

    fun handle(input: InputEvent) {
        when (input) {
            is MotionEvent -> {
                controllerStateFlow.update { controllerState ->
                    controllerState.copy(
                        r2 = input.getAxisValue(AXIS_GAS),
                        l2 = input.getAxisValue(AXIS_BRAKE),
                        dpadX = input.getAxisValue(AXIS_HAT_X),
                        dpadY = input.getAxisValue(AXIS_HAT_Y),
                        leftStickX = input.getAxisValue(AXIS_X),
                        leftStickY = input.getAxisValue(AXIS_Y),
                        rightStickX = input.getAxisValue(AXIS_Z),
                        rightStickY = input.getAxisValue(AXIS_RZ),
                    )
                }
            }

            is KeyEvent -> {
                controllerStateFlow.update { controllerState ->
                    when (input.keyCode) {
                        KEYCODE_BUTTON_A -> controllerState.copy(a = input.action == ACTION_DOWN)
                        KEYCODE_BUTTON_B -> controllerState.copy(b = input.action == ACTION_DOWN)
                        KEYCODE_BUTTON_X -> controllerState.copy(x = input.action == ACTION_DOWN)
                        KEYCODE_BUTTON_Y -> controllerState.copy(y = input.action == ACTION_DOWN)
                        KEYCODE_BUTTON_L1 -> controllerState.copy(l1 = input.action == ACTION_DOWN)
                        KEYCODE_BUTTON_R1 -> controllerState.copy(r1 = input.action == ACTION_DOWN)
                        KEYCODE_BUTTON_THUMBL -> controllerState.copy(l3 = input.action == ACTION_DOWN)
                        KEYCODE_BUTTON_THUMBR -> controllerState.copy(r3 = input.action == ACTION_DOWN)
                        KEYCODE_BUTTON_START -> controllerState.copy(start = input.action == ACTION_DOWN)
                        KEYCODE_BUTTON_SELECT -> controllerState.copy(select = input.action == ACTION_DOWN)
                        KEYCODE_BACK -> controllerState.copy(guide = input.action == ACTION_DOWN)
                        else -> controllerState
                    }
                }
            }
        }
    }

}