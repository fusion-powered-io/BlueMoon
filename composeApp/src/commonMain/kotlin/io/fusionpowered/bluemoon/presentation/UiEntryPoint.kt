package io.fusionpowered.bluemoon.presentation

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.navigation3.ui.NavDisplay
import io.fusionpowered.bluemoon.presentation.components.BlueMoonScaffold
import io.fusionpowered.bluemoon.presentation.components.BluetoothController
import io.fusionpowered.bluemoon.presentation.components.BluetoothKeyboard
import io.fusionpowered.bluemoon.presentation.components.BluetoothTouchpad
import io.fusionpowered.bluemoon.presentation.components.BluetoothVolume
import io.fusionpowered.bluemoon.presentation.navigation.Navigator
import io.fusionpowered.bluemoon.presentation.theme.BlueMoonTheme
import io.fusionpowered.bluemoon.presentation.views.deviceselector.DeviceSelector
import org.koin.compose.koinInject
import org.koin.compose.module.rememberKoinModules
import org.koin.compose.navigation3.koinEntryProvider
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation


@Composable
fun UiEntryPoint(
    navigator: Navigator = koinInject(),
    bluetoothControllerPresenter: @Composable () -> BluetoothController.State = { BluetoothController.present() },
    bluetoothVolumePresenter: @Composable () -> BluetoothVolume.State = { BluetoothVolume.present() },
    deviceSelectorPresenter: @Composable () -> DeviceSelector.State = { DeviceSelector.present() },
    bluemoonScaffoldPresenter: @Composable () -> BlueMoonScaffold.State = { BlueMoonScaffold.present() },
    touchpadPresenter: @Composable () -> BluetoothTouchpad.State = { BluetoothTouchpad.present() },
    keyboardPresenter: @Composable () -> BluetoothKeyboard.State = { BluetoothKeyboard.present() },
) =
    BlueMoonTheme {
        Surface {
            bluetoothControllerPresenter()
            bluetoothVolumePresenter()

            rememberKoinModules {
                listOf(
                    module {
                        navigation<DeviceSelector.Screen> { _ ->
                            DeviceSelector(
                                presenter = deviceSelectorPresenter
                            )
                        }
                    }
                )
            }

            BlueMoonScaffold(
                presenter = bluemoonScaffoldPresenter,
                touchpadPresenter = touchpadPresenter,
                keyboardPresenter = keyboardPresenter
            ) {
                NavDisplay(
                    backStack = navigator.backStack,
                    entryProvider = koinEntryProvider(),
                    onBack = { navigator.goBack() }
                )
            }
        }
    }


