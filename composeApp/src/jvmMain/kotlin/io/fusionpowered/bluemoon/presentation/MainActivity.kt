package io.fusionpowered.bluemoon.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Window
import io.fusionpowered.bluemoon.domain.controller.ControllerInputHandler
import org.koin.compose.koinInject
import org.koin.core.annotation.KoinExperimentalAPI


@OptIn(KoinExperimentalAPI::class)
@Composable
context(applicationScope: ApplicationScope)
fun mainActivity(
    controllerInputHandler: ControllerInputHandler = koinInject(),
) {
    Window(
        onCloseRequest = applicationScope::exitApplication,
        title = "BlueMoon",
    ) {
        ViewEntryPoint()
        controllerInputHandler.handle("test")
    }
}