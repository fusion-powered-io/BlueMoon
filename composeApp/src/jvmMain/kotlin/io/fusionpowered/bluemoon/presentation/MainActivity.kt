package io.fusionpowered.bluemoon.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Window


@Composable
context(applicationScope: ApplicationScope)
fun MainActivity() {
    Window(
        onCloseRequest = applicationScope::exitApplication,
        title = "BlueMoon",
    ) {
        UiEntryPoint()
    }
}