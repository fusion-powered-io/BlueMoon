package io.fusionpowered.bluemoon.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Window
import org.koin.core.annotation.KoinExperimentalAPI


@OptIn(KoinExperimentalAPI::class)
@Composable
context(applicationScope: ApplicationScope)
fun mainActivity() {
    Window(
        onCloseRequest = applicationScope::exitApplication,
        title = "BlueMoon",
    ) {
        UiEntryPoint()
    }
}