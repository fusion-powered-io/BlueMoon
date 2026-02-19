package io.fusionpowered.bluemoon.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.navigation3.ui.NavDisplay
import bluemoon.composeapp.generated.resources.Res
import bluemoon.composeapp.generated.resources.background
import io.fusionpowered.bluemoon.bootstrap.KoinPresenter
import io.fusionpowered.bluemoon.bootstrap.injectPresenter
import io.fusionpowered.bluemoon.bootstrap.presenter
import io.fusionpowered.bluemoon.presentation.components.BlueMoonScaffold
import io.fusionpowered.bluemoon.presentation.components.BlueMoonScaffold.State.SheetContent.Keyboard
import io.fusionpowered.bluemoon.presentation.components.BlueMoonScaffold.State.SheetContent.Touchpad
import io.fusionpowered.bluemoon.presentation.components.BluetoothController
import io.fusionpowered.bluemoon.presentation.components.BluetoothVolume
import io.fusionpowered.bluemoon.presentation.navigation.Navigator
import io.fusionpowered.bluemoon.presentation.preview.PreviewApplication
import io.fusionpowered.bluemoon.presentation.theme.BlueMoonTheme
import io.fusionpowered.bluemoon.presentation.views.deviceselector.DeviceSelector
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import org.koin.compose.module.rememberKoinModules
import org.koin.compose.navigation3.koinEntryProvider
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation


@Composable
fun UiEntryPoint(
    navigator: Navigator = koinInject(),
    bluetoothControllerPresenter: KoinPresenter<BluetoothController.State> = injectPresenter<BluetoothController.State>(),
    bluetoothVolumePresenter: KoinPresenter<BluetoothVolume.State> = injectPresenter<BluetoothVolume.State>(),
) =
    BlueMoonTheme {
        Surface {
            bluetoothControllerPresenter.present()
            bluetoothVolumePresenter.present()

            rememberKoinModules {
                listOf(
                    module {
                        navigation<DeviceSelector.Screen> { _ ->
                            DeviceSelector()
                        }
                    }
                )
            }

            Background()
            BlueMoonScaffold {
                NavDisplay(
                    backStack = navigator.backStack,
                    entryProvider = koinEntryProvider(),
                    onBack = { navigator.goBack() }
                )
            }
        }
    }

@Composable
private fun Background() {
    Image(
        painter = painterResource(Res.drawable.background),
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop,
        contentDescription = null
    )
}


@Preview(
    heightDp = 480,
    widthDp = 640,
)
@Composable
private fun UiEntryPointPreview( ) =
    PreviewApplication {
        UiEntryPoint()
    }

@Preview(
    heightDp = 480,
    widthDp = 640,
)
@Composable
private fun UiEntryPointExpandedTouchpadPreview() =
    PreviewApplication(
        additionalModules = {
            presenter<BlueMoonScaffold.State> {
                BlueMoonScaffold.State.Normal(
                    sheetContent = remember { mutableStateOf(Touchpad) },
                    scaffoldState = rememberBottomSheetScaffoldState().apply {
                        runBlocking {
                            bottomSheetState.expand()
                        }
                    }
                )
            }
        }
    ) {
        UiEntryPoint()
    }

@Preview(
    heightDp = 480,
    widthDp = 640,
)
@Composable
private fun UiEntryPointExpandedKeyboardPreview() =
    PreviewApplication(
        additionalModules = {
            presenter<BlueMoonScaffold.State> {
                BlueMoonScaffold.State.Normal(
                    sheetContent = remember { mutableStateOf(Keyboard) },
                    scaffoldState = rememberBottomSheetScaffoldState().apply {
                        runBlocking {
                            bottomSheetState.expand()
                        }
                    }
                )
            }
        }
    ) {
        UiEntryPoint()
    }
