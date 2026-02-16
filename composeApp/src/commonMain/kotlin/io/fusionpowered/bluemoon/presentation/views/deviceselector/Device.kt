package io.fusionpowered.bluemoon.presentation.views.deviceselector

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import compose.icons.TablerIcons
import compose.icons.tablericons.Bluetooth
import compose.icons.tablericons.DeviceLaptop
import compose.icons.tablericons.DeviceMobile
import io.fusionpowered.bluemoon.domain.bluetooth.BluetoothClient
import io.fusionpowered.bluemoon.domain.bluetooth.model.BluetoothDevice
import io.fusionpowered.bluemoon.domain.bluetooth.model.BluetoothDevice.MajorClass.COMPUTER
import io.fusionpowered.bluemoon.domain.bluetooth.model.BluetoothDevice.MajorClass.PHONE
import io.fusionpowered.bluemoon.domain.bluetooth.model.ConnectionState
import io.fusionpowered.bluemoon.presentation.modifier.specularShine
import io.fusionpowered.bluemoon.presentation.views.deviceselector.Device.State
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import kotlin.math.absoluteValue

object Device {

    @Composable
    fun present(
        device: BluetoothDevice,
        bluetoothClient: BluetoothClient = koinInject(),
        coroutineScope: CoroutineScope = rememberCoroutineScope(),
    ): State {
        val connectionState by bluetoothClient.connectionStateFlow
            .collectAsStateWithLifecycle(ConnectionState.Disconnected)

        return when (val it = connectionState) {
            is ConnectionState.Connecting if (it.device == device) -> {
                State.Connecting(
                    device = it.device,
                    onClick = { bluetoothClient.disconnect(device) }
                )
            }

            is ConnectionState.Connected if (it.device == device) -> {
                State.Connected(
                    device = it.device,
                    onClick = { bluetoothClient.disconnect(device) }
                )
            }

            is ConnectionState.Connected if (it.device != device) -> {
                State.Disconnected(
                    device = device,
                    onClick = {
                        bluetoothClient.disconnect(it.device)
                        coroutineScope.launch {
                            delay(500) // Give it some time to disconnect
                            bluetoothClient.connect(device)
                        }
                    }
                )
            }

            else -> {
                State.Disconnected(
                    device = device,
                    onClick = {
                        coroutineScope.launch {
                            bluetoothClient.connect(device)
                        }
                    }
                )
            }
        }
    }

    sealed interface State {

        val device: BluetoothDevice
        val onClick: () -> Unit


        data class Disconnected(
            override val device: BluetoothDevice,
            override val onClick: () -> Unit = {},
        ) : State

        data class Connecting(
            override val device: BluetoothDevice,
            override val onClick: () -> Unit = {},
        ) : State

        data class Connected(
            override val device: BluetoothDevice,
            override val onClick: () -> Unit = {},
        ) : State

    }

    @Composable
    operator fun invoke(
        modifier: Modifier = Modifier,
        presenter: @Composable () -> State,
    ) {
        val state = presenter()
        val scope = rememberCoroutineScope()
        val density = LocalDensity.current.density

        val baseColor = when (state) {
            is State.Connected -> Color(0xFF00FFCC)
            is State.Connecting -> Color(0xFFFFCC00)
            else -> Color(0xFF4A90E2)
        }
        val animatedColor by animateColorAsState(baseColor, tween(300), label = "color")

        val swipeOffset = remember { Animatable(0f) }
        val nudgeOffset = remember { Animatable(0f) }
        val maxSwipe = 120f * density

        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(86.dp)
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    scope.launch {
                        nudgeOffset.animateTo(-15f * density, tween(100, easing = LinearOutSlowInEasing))
                        nudgeOffset.animateTo(0f, spring(Spring.DampingRatioHighBouncy))
                    }
                }
                .draggable(
                    state = rememberDraggableState { delta ->
                        scope.launch {
                            swipeOffset.snapTo((swipeOffset.value + delta).coerceIn(-maxSwipe, 0f))
                        }
                    },
                    orientation = Orientation.Horizontal,
                    onDragStopped = { velocity ->
                        val triggerThreshold = maxSwipe * 0.6f
                        val shouldConnect = swipeOffset.value.absoluteValue > triggerThreshold || velocity < -500f

                        if (shouldConnect) {
                            state.onClick()
                        }

                        scope.launch {
                            swipeOffset.animateTo(
                                targetValue = 0f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                        }
                    }
                )
                .graphicsLayer {
                    val totalX = swipeOffset.value + nudgeOffset.value
                    val progress = totalX / maxSwipe

                    translationX = totalX
                    rotationY = progress * 12f
                    cameraDistance = 10f * density

                    // Visual "push-back" depth
                    val scale = 1f - (progress.absoluteValue * 0.04f)
                    scaleX = scale
                    scaleY = scale
                }
                .background(Color.White.copy(alpha = 0.05f), RectangleShape)
                .border(0.5.dp, Color.White.copy(alpha = 0.12f), RectangleShape)
        ) {
            val totalOffset = (swipeOffset.value + nudgeOffset.value).absoluteValue
            val progress = (totalOffset / maxSwipe).coerceIn(0f, 1f)
            Row(
                modifier = Modifier
                    .specularShine(progress)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DeviceClassIcon(state, animatedColor)
                Spacer(Modifier.width(16.dp))
                DeviceInformation(state, animatedColor)
                InstructionalSwipeMessage(progress, state, animatedColor)
                Spacer(Modifier.width(8.dp))
                StatusDot(animatedColor)
            }
        }
    }



    @Composable
    private fun DeviceClassIcon(
        state: State,
        animatedColor: Color,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = when (state.device.majorClass) {
                    COMPUTER -> TablerIcons.DeviceLaptop
                    PHONE -> TablerIcons.DeviceMobile
                    else -> TablerIcons.Bluetooth
                },
                contentDescription = null,
                tint = animatedColor,
                modifier = Modifier.size(36.dp)
            )
            Box(Modifier.size(48.dp).drawBehind {
                drawCircle(animatedColor.copy(alpha = 0.1f))
            })
        }
    }

    @Composable
    private fun RowScope.DeviceInformation(
        state: State,
        animatedColor: Color,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = state.device.name.uppercase(),
                color = Color.White.copy(alpha = 0.9f),
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                letterSpacing = 1.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = state.device.mac,
                color = animatedColor.copy(alpha = 0.5f),
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp
            )
        }
    }

    @Composable
    private fun StatusDot(animatedColor: Color) {
        Box(
            Modifier
                .size(6.dp)
                .background(animatedColor, CircleShape)
        )
    }

    @Composable
    private fun InstructionalSwipeMessage(
        progress: Float,
        state: State,
        animatedColor: Color,
    ) {
        if (progress > 0.1f) {
            Text(
                text = when (state) {
                    is State.Connected -> "Swipe to Disconnect"
                    is State.Connecting -> "Swipe to Disconnect"
                    else -> "Swipe to Connect"
                },
                color = animatedColor.copy(alpha = progress.coerceIn(0f, 0.6f)),
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black
                ),
                modifier = Modifier.graphicsLayer { alpha = progress }
            )
        }
    }

}


@Preview
@Composable
fun DevicePreview() {
    Device(
        presenter = {
            State.Connected(
                device = BluetoothDevice(
                    name = "test device",
                    mac = "00:00:00:00:00:00",
                    majorClass = COMPUTER
                )
            )
        }
    )
}