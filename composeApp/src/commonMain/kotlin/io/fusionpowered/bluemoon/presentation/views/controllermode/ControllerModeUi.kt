package io.fusionpowered.bluemoon.presentation.views.controllermode

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.fusionpowered.bluemoon.presentation.views.controllermode.ControllerModeScreen.State

@Composable
fun ControllerModeUi(
    state: State,
) =
    when (state) {
        is State.Connecting -> {
            Text("Connecting to ${state.deviceName}")
        }
    }


@Preview
@Composable
fun ControllerModeUiPreview() {
    ControllerModeUi(
        state = State.Connecting(
            deviceName = "test device 1"
        )
    )

}