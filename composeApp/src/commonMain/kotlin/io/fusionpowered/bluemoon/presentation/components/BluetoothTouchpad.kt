package io.fusionpowered.bluemoon.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.fusionpowered.bluemoon.domain.bluetooth.BluetoothClient
import io.fusionpowered.bluemoon.domain.bluetooth.model.ConnectionState
import io.fusionpowered.bluemoon.domain.touchpad.model.TouchpadState
import org.koin.compose.koinInject

object BluetoothTouchpad {

    const val MOUSE_SENSITIVITY = 1.5f
    const val SCROLL_SCALING = 0.1f

    @Composable
    fun present(
        bluetoothClient: BluetoothClient = koinInject(),
    ): State {
        val connectionState by bluetoothClient.connectionStateFlow.collectAsStateWithLifecycle()
        val haptic = LocalHapticFeedback.current

        return when (val connection = connectionState) {
            is ConnectionState.Connected -> {
                var activePointerId by remember { mutableStateOf(PointerId(-1)) }
                var lastX by remember { mutableFloatStateOf(0f) }
                var lastY by remember { mutableFloatStateOf(0f) }
                var scrollLastY by remember { mutableFloatStateOf(0f) }
                var leftDownAt by remember { mutableLongStateOf(0L) }
                var tapAt by remember { mutableLongStateOf(0L) }
                var isTapDragging by remember { mutableStateOf(false) }

                // Sub-pixel accumulation for slow movements
                var pendingDX by remember { mutableFloatStateOf(0f) }
                var pendingDY by remember { mutableFloatStateOf(0f) }
                var pendingScroll by remember { mutableFloatStateOf(0f) }

                State(
                    onPointerInput = {
                        this.awaitPointerEventScope {
                            while (true) {
                                val downEvent = awaitPointerEvent(PointerEventPass.Initial)
                                val downChanges = downEvent.changes

                                if (downChanges.size >= 2) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    bluetoothClient.send(connection.device, TouchpadState(rightButton = true))
                                    bluetoothClient.send(connection.device, TouchpadState(rightButton = false))
                                    downChanges.forEach { it.consume() }
                                    continue
                                }

                                val downChange = downChanges.first()
                                activePointerId = downChange.id
                                leftDownAt = System.currentTimeMillis()
                                lastX = downChange.position.x
                                lastY = downChange.position.y
                                scrollLastY = lastY

                                // Reset sub-pixel accumulators on new touch
                                pendingDX = 0f
                                pendingDY = 0f
                                pendingScroll = 0f

                                if ((System.currentTimeMillis() - tapAt) in (21..149)) {
                                    isTapDragging = true
                                    bluetoothClient.send(connection.device, TouchpadState(leftButton = true))
                                }

                                downChange.consume()

                                var pointerActive = true
                                while (pointerActive) {
                                    val event = awaitPointerEvent(PointerEventPass.Initial)
                                    val changes = event.changes

                                    // --- SCROLL LOGIC ---
                                    if (changes.size >= 2 && changes.all { it.pressed }) {
                                        val currentAvgY = changes.map { it.position.y }.average().toFloat()

                                        // Accumulate scroll delta
                                        pendingScroll += (scrollLastY - currentAvgY) * SCROLL_SCALING
                                        val scrollAmount = pendingScroll.toInt()

                                        if (scrollAmount != 0) {
                                            bluetoothClient.send(connection.device, TouchpadState(wheel = scrollAmount))
                                            pendingScroll -= scrollAmount // Keep the remainder
                                        }
                                        scrollLastY = currentAvgY
                                        changes.forEach { it.consume() }
                                        continue
                                    }

                                    // --- MOVEMENT LOGIC ---
                                    val mainChange = changes.find { it.id == activePointerId }

                                    if (mainChange == null || !mainChange.pressed) {
                                        pointerActive = false
                                        val now = System.currentTimeMillis()
                                        if (isTapDragging) {
                                            isTapDragging = false
                                            bluetoothClient.send(connection.device, TouchpadState(leftButton = false))
                                        } else if ((now - leftDownAt) in (21..149)) {
                                            tapAt = now
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            bluetoothClient.send(connection.device, TouchpadState(leftButton = true))
                                            bluetoothClient.send(connection.device, TouchpadState(leftButton = false))
                                        }
                                        activePointerId = PointerId(-1)
                                    } else {
                                        val x = mainChange.position.x
                                        val y = mainChange.position.y

                                        // Accumulate movement with sensitivity
                                        pendingDX += (x - lastX) * MOUSE_SENSITIVITY
                                        pendingDY += (y - lastY) * MOUSE_SENSITIVITY

                                        val outDX = pendingDX.toInt()
                                        val outDY = pendingDY.toInt()

                                        if (outDX != 0 || outDY != 0) {
                                            bluetoothClient.send(
                                                connection.device, TouchpadState(
                                                    deltaX = outDX,
                                                    deltaY = outDY,
                                                    leftButton = isTapDragging
                                                )
                                            )
                                            pendingDX -= outDX
                                            pendingDY -= outDY
                                        }

                                        lastX = x
                                        lastY = y
                                        scrollLastY = y
                                        mainChange.consume()
                                    }
                                }
                            }
                        }
                    }
                )
            }

            else -> State(
                onPointerInput = {}
            )
        }
    }

    data class State(
        val onPointerInput: suspend PointerInputScope.() -> Unit,
    )

    @Composable
    operator fun invoke(
        modifier: Modifier = Modifier,
        presenter: @Composable () -> State,
    ) {
        val state = presenter()
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(300.dp)
                .padding(3.dp)
                .clip(shape = RoundedCornerShape(8.dp))
                .background(color = Color(0xFF3E4759))
                .pointerInput(state, state.onPointerInput),
            contentAlignment = Alignment.Center
        ) {
        }
    }

}
