package io.fusionpowered.bluemoon.presentation.views.controllermode

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.fusionpowered.bluemoon.domain.controller.model.ControllerState
import io.fusionpowered.bluemoon.presentation.views.controllermode.ControllerModeScreen.State

@Composable
fun ControllerModeUi(
    state: State,
) =
    when (state) {
        State.Disconnected -> {}

        is State.Connecting -> {
            Text("Connecting to ${state.deviceName}")
        }

        is State.Connected -> Column {
            Text("Connected to ${state.deviceName}")
            Text(state.controllerState.toString())
        }

    }


@Preview
@Composable
fun ControllerModeUiPreview() {
    ControllerModeUi(
        state = State.Connected(
            deviceName = "test device 1",
            controllerState = ControllerState()
        )
    )

}