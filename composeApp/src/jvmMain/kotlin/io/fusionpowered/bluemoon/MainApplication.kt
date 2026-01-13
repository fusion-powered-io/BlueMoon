package io.fusionpowered.bluemoon

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.fusionpowered.bluemoon.adapter.ui.App
import org.koin.core.context.GlobalContext
import org.koin.ksp.generated.module

fun main() {
    GlobalContext.startKoin {
        modules(AppModule().module)
    }
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "BlueMoon",
        ) {
            App()
        }
    }
}