package io.fusionpowered.bluemoon

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.fusionpowered.bluemoon.presentation.UiEntryPoint

fun main() {
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "BlueMoon",
        ) {
            UiEntryPoint()
        }
    }
}