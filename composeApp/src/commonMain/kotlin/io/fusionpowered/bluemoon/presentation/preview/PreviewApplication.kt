package io.fusionpowered.bluemoon.presentation.preview

import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import io.fusionpowered.bluemoon.bootstrap.presenter
import io.fusionpowered.bluemoon.domain.bluetooth.model.BluetoothDevice
import io.fusionpowered.bluemoon.domain.bluetooth.model.BluetoothDevice.MajorClass.COMPUTER
import io.fusionpowered.bluemoon.domain.bluetooth.model.BluetoothDevice.MajorClass.PHONE
import io.fusionpowered.bluemoon.presentation.components.*
import io.fusionpowered.bluemoon.presentation.components.BlueMoonScaffold.State.SheetContent
import io.fusionpowered.bluemoon.presentation.navigation.Navigator
import io.fusionpowered.bluemoon.presentation.theme.BlueMoonTheme
import io.fusionpowered.bluemoon.presentation.views.deviceselector.Device
import io.fusionpowered.bluemoon.presentation.views.deviceselector.DeviceSelector
import org.koin.compose.KoinApplicationPreview
import org.koin.core.module.Module
import org.koin.dsl.module


@Composable
fun PreviewApplication(
    additionalModules: Module.() -> Unit = {},
    content: @Composable () -> Unit
) =
    KoinApplicationPreview(
        application = {
            allowOverride(true)
            modules(
                module {
                    single { Navigator() }

                    presenter<BluetoothController.State> {
                        BluetoothController.State
                    }

                    presenter<BluetoothVolume.State> {
                        BluetoothVolume.State
                    }

                    presenter<BlueMoonScaffold.State> {
                        BlueMoonScaffold.State.Normal(
                            sheetContent = remember { mutableStateOf(SheetContent.Touchpad) },
                            scaffoldState = rememberBottomSheetScaffoldState()
                        )
                    }

                    presenter<Device.State> { parameters ->
                        Device.State.Connected(
                            device = parameters.component1()
                        )
                    }

                    presenter<DeviceSelector.State> {
                        DeviceSelector.State(
                            availableDevices = setOf(
                                BluetoothDevice(
                                    name = "test device 2",
                                    mac = "00:00:00:00:00:01",
                                    majorClass = COMPUTER
                                ),
                                BluetoothDevice(
                                    name = "test device 3",
                                    mac = "00:00:00:00:00:02",
                                    majorClass = PHONE
                                )
                            )
                        )
                    }

                    presenter<BluetoothTouchpad.State> {
                        BluetoothTouchpad.State()
                    }

                    presenter<BluetoothKeyboard.State> {
                        BluetoothKeyboard.State.Connected(
                            getDisplayLabel = { info -> info.label },
                            isHighlighted = { false }
                        )
                    }

                    additionalModules()
                }
            )
        }
    ) {
        BlueMoonTheme {
            content()
        }
    }