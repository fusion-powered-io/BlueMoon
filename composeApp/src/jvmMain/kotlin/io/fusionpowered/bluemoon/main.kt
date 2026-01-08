package io.fusionpowered.bluemoon

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.fusionpowered.bluemoon.di.initKoin

fun main() {
    initKoin()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "BlueMoon",
        ) {
            App()
        }
    }
}