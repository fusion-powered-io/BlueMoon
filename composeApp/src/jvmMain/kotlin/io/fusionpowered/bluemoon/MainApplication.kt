package io.fusionpowered.bluemoon

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.fusionpowered.bluemoon.adapter.ui.App
import org.koin.compose.koinInject
import org.koin.core.annotation.KoinApplication
import org.koin.ksp.generated.startKoin


@KoinApplication
object MainApplication

fun main() {
    MainApplication.startKoin {
        printLogger()
    }
    application {
        val controllerInputHandler = koinInject<ControllerInputHandler>()
        Window(
            onCloseRequest = ::exitApplication,
            title = "BlueMoon",
        ) {
            App()
            controllerInputHandler.handle("test")
        }
    }
}