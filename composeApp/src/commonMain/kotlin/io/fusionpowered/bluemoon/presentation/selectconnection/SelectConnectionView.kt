package io.fusionpowered.bluemoon.presentation.selectconnection

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black

@Composable
fun SelectConnectionView(
    navigateToControllerMode: (connection: String) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .background(Black)
            .safeContentPadding()
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Select Connection", color = Color.White)
        Button(
            onClick = { navigateToControllerMode("test") },
        ) {
            Text("connect")
        }
    }
}