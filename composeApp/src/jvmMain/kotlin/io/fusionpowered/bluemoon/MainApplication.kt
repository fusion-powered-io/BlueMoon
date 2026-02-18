package io.fusionpowered.bluemoon

import androidx.compose.ui.window.application
import io.fusionpowered.bluemoon.presentation.MainActivity
import org.koin.core.annotation.KoinApplication
import org.koin.ksp.generated.startKoin


@KoinApplication
object MainApplication

fun main() {
    MainApplication.startKoin {
        printLogger()
    }

    application {
        MainActivity()
    }
}

