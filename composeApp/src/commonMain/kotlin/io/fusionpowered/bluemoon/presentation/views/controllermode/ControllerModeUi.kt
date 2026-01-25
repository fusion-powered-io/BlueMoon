package io.fusionpowered.bluemoon.presentation.views.controllermode

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import io.fusionpowered.bluemoon.domain.controller.model.ControllerState
import io.fusionpowered.bluemoon.presentation.views.controllermode.ControllerModeScreen.State

@Composable
fun ControllerModeUi(
    modifier: Modifier = Modifier,
    state: State,
) =
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = when (state) {
                is State.Disconnected -> "Disconnected"
                is State.Connecting -> "Connecting to ${state.deviceName}"
                is State.Connected -> "Connected to ${state.deviceName}"
            },
            color = Color.White,
            textAlign = TextAlign.Center
        )
        if (state is State.Disconnected) {
            state.onDisconnected()
        }
    }


@Preview
@Composable
fun ControllerModeUiPreview() {
    ControllerModeUi(
        modifier = Modifier
            .background(Color.Black)
            .fillMaxSize(),
        state = State.Connected(
            deviceName = "test device 1",
            controllerState = ControllerState()
        )
    )

}