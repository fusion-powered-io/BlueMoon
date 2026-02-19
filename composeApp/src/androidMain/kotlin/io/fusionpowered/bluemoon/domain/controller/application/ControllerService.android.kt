package io.fusionpowered.bluemoon.domain.controller.application

import android.view.InputEvent
import android.view.KeyEvent
import android.view.KeyEvent.*
import android.view.KeyEvent.ACTION_DOWN
import android.view.MotionEvent
import android.view.MotionEvent.AXIS_BRAKE
import android.view.MotionEvent.AXIS_GAS
import android.view.MotionEvent.AXIS_HAT_X
import android.view.MotionEvent.AXIS_HAT_Y
import android.view.MotionEvent.AXIS_RZ
import android.view.MotionEvent.AXIS_X
import android.view.MotionEvent.AXIS_Y
import android.view.MotionEvent.AXIS_Z
import io.fusionpowered.bluemoon.domain.controller.ControllerClient
import io.fusionpowered.bluemoon.domain.controller.model.ControllerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import kotlin.time.Clock

@Single
actual class ControllerService actual constructor() : ControllerClient, KoinComponent {

    @Volatile
    private var controllerState = ControllerState()

    override val controllerStateFlow: StateFlow<ControllerState> = flow {
        while (true) {
            emit(controllerState.copy(id = Clock.System.now().toEpochMilliseconds()))
            delay(4) // 250hz
        }
    }.stateIn(
        scope = CoroutineScope(Dispatchers.Default),
        started = SharingStarted.Eagerly,
        initialValue = controllerState
    )

    fun handle(input: InputEvent) {
        when (input) {
            is MotionEvent -> {
                controllerState = controllerState.copy(
                    id = input.eventTime,
                    r2Axis = input.getAxisValue(AXIS_GAS),
                    l2Axis = input.getAxisValue(AXIS_BRAKE),
                    dpadX = input.getAxisValue(AXIS_HAT_X),
                    dpadY = input.getAxisValue(AXIS_HAT_Y),
                    leftStickX = input.getAxisValue(AXIS_X),
                    leftStickY = input.getAxisValue(AXIS_Y),
                    rightStickX = input.getAxisValue(AXIS_Z),
                    rightStickY = input.getAxisValue(AXIS_RZ),
                )
            }

            is KeyEvent -> {
                val isDown = input.action == ACTION_DOWN
                controllerState = when (input.keyCode) {
                    KEYCODE_BUTTON_A -> controllerState.copy(a = isDown)
                    KEYCODE_BUTTON_B -> controllerState.copy(b = isDown)
                    KEYCODE_BUTTON_X -> controllerState.copy(x = isDown)
                    KEYCODE_BUTTON_Y -> controllerState.copy(y = isDown)
                    KEYCODE_BUTTON_L1 -> controllerState.copy(l1 = isDown)
                    KEYCODE_BUTTON_R1 -> controllerState.copy(r1 = isDown)
                    KEYCODE_BUTTON_L2 -> controllerState.copy(l2Button = isDown)
                    KEYCODE_BUTTON_R2 -> controllerState.copy(r2Button = isDown)
                    KEYCODE_BUTTON_THUMBL -> controllerState.copy(l3 = isDown)
                    KEYCODE_BUTTON_THUMBR -> controllerState.copy(r3 = isDown)
                    KEYCODE_BUTTON_START -> controllerState.copy(start = isDown)
                    KEYCODE_BUTTON_SELECT -> controllerState.copy(select = isDown)
                    KEYCODE_BACK -> controllerState.copy(guide = isDown)
                    else -> controllerState
                }
            }
        }
    }
}