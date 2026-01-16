package io.fusionpowered.bluemoon.presentation

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.ui.NavDisplay
import io.fusionpowered.bluemoon.presentation.controllermode.ControllerModeRoute
import io.fusionpowered.bluemoon.presentation.controllermode.ControllerModeView
import io.fusionpowered.bluemoon.presentation.selectconnection.SelectConnectionRoute
import io.fusionpowered.bluemoon.presentation.selectconnection.SelectConnectionView
import org.koin.compose.module.rememberKoinModules
import org.koin.compose.navigation3.koinEntryProvider
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
@Composable
fun ViewEntryPoint(
    backStack: SnapshotStateList<Any> = remember { mutableStateListOf(SelectConnectionRoute) },
) {
    rememberKoinModules {
        listOf(
            module {
                navigation<SelectConnectionRoute> { _ ->
                    SelectConnectionView(
                        navigateToControllerMode = { deviceConnection ->
                            backStack.add(ControllerModeRoute(deviceConnection))
                        }
                    )
                }

                navigation<ControllerModeRoute> { route ->
                    ControllerModeView(
                        connection = route.connection
                    )
                }
            }
        )
    }
    MaterialTheme {
        NavDisplay(
            backStack = backStack,
            entryProvider = koinEntryProvider(),
            onBack = { backStack.removeLastOrNull() }
        )
    }
}

@Composable
fun SelectConnectionView() {
    TODO("Not yet implemented")
}