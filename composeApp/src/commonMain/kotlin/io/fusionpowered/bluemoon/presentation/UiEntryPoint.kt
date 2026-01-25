package io.fusionpowered.bluemoon.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation3.ui.NavDisplay
import io.fusionpowered.bluemoon.presentation.navigation.Navigator
import io.fusionpowered.bluemoon.presentation.theme.BlueMoonTheme
import io.fusionpowered.bluemoon.presentation.views.controllermode.ControllerModeScreen
import io.fusionpowered.bluemoon.presentation.views.controllermode.ControllerModeUi
import io.fusionpowered.bluemoon.presentation.views.controllermode.presentControllerMode
import io.fusionpowered.bluemoon.presentation.views.selectconnection.SelectConnectionScreen
import io.fusionpowered.bluemoon.presentation.views.selectconnection.SelectConnectionUi
import io.fusionpowered.bluemoon.presentation.views.selectconnection.presentSelectConnection
import org.koin.compose.koinInject
import org.koin.compose.module.rememberKoinModules
import org.koin.compose.navigation3.koinEntryProvider
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
@Composable
fun UiEntryPoint(
    navigator: Navigator = koinInject()
) =
    BlueMoonTheme {
        rememberKoinModules {
            listOf(
                module {
                    navigation<SelectConnectionScreen> { _ ->
                        SelectConnectionUi(
                            state = presentSelectConnection()
                        )
                    }

                    navigation<ControllerModeScreen> { screen ->
                        ControllerModeUi(
                            modifier = Modifier
                                .background(color = Color.Black)
                                .fillMaxSize(),
                            state = presentControllerMode(screen.device)
                        )
                    }
                }
            )
        }

        NavDisplay(
            backStack = navigator.backStack,
            entryProvider = koinEntryProvider(),
            onBack = { navigator.goBack() }
        )
    }
