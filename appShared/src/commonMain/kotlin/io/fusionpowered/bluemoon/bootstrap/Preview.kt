package io.fusionpowered.bluemoon.bootstrap

import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import io.fusionpowered.bluemoon.domain.bluetooth.model.BluetoothDevice
import io.fusionpowered.bluemoon.domain.bluetooth.model.BluetoothDevice.MajorClass.COMPUTER
import io.fusionpowered.bluemoon.domain.bluetooth.model.BluetoothDevice.MajorClass.PHONE
import io.fusionpowered.bluemoon.presentation.component.headless.BluetoothController
import io.fusionpowered.bluemoon.presentation.component.headless.BluetoothVolume
import io.fusionpowered.bluemoon.presentation.component.layout.HomeLayout
import io.fusionpowered.bluemoon.presentation.component.layout.HomeLayout.State.SheetContent.Touchpad
import io.fusionpowered.bluemoon.presentation.component.widget.BluetoothKeyboard
import io.fusionpowered.bluemoon.presentation.component.widget.BluetoothTouchpad
import io.fusionpowered.bluemoon.presentation.component.widget.DeviceCard
import io.fusionpowered.bluemoon.presentation.page.Home
import io.fusionpowered.bluemoon.presentation.theme.BlueMoonTheme
import org.koin.compose.KoinApplicationPreview
import org.koin.core.module.Module
import org.koin.dsl.module


@Composable
fun PreviewApplication(
    overrides: Module.() -> Unit = {},
    content: @Composable () -> Unit,
) =
    KoinApplicationPreview(
        application = {
            allowOverride(true)
            modules(
                module {
                    presenter<BluetoothController.State> {
                        BluetoothController.State
                    }

                    presenter<BluetoothVolume.State> {
                        BluetoothVolume.State
                    }

                    presenter<HomeLayout.State> {
                        HomeLayout.State.Normal(
                            sheetContent = remember { mutableStateOf(Touchpad) },
                            scaffoldState = rememberBottomSheetScaffoldState()
                        )
                    }

                    presenter<DeviceCard.State> { parameters ->
                        DeviceCard.State.Connected(
                            device = parameters.component1()
                        )
                    }

                    presenter<Home.State> {
                        Home.State(
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

                    overrides()
                }
            )
        }
    ) {
        BlueMoonTheme {
            content()
        }
    }