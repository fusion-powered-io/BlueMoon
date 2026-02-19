package io.fusionpowered.bluemoon.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SheetValue.Expanded
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import bluemoon.composeapp.generated.resources.Res
import bluemoon.composeapp.generated.resources.background
import io.fusionpowered.bluemoon.bootstrap.KoinPresenter
import io.fusionpowered.bluemoon.bootstrap.PreviewApplication
import io.fusionpowered.bluemoon.bootstrap.injectPresenter
import io.fusionpowered.bluemoon.bootstrap.presenter
import io.fusionpowered.bluemoon.presentation.components.BlueMoonScaffold
import io.fusionpowered.bluemoon.presentation.components.BlueMoonScaffold.State.SheetContent.Keyboard
import io.fusionpowered.bluemoon.presentation.components.BlueMoonScaffold.State.SheetContent.Touchpad
import io.fusionpowered.bluemoon.presentation.components.BluetoothController
import io.fusionpowered.bluemoon.presentation.components.BluetoothVolume
import io.fusionpowered.bluemoon.presentation.components.DeviceSelector
import io.fusionpowered.bluemoon.presentation.theme.BlueMoonTheme
import org.jetbrains.compose.resources.painterResource


@Composable
fun UiEntryPoint(
    bluetoothControllerPresenter: KoinPresenter<BluetoothController.State> = injectPresenter<BluetoothController.State>(),
    bluetoothVolumePresenter: KoinPresenter<BluetoothVolume.State> = injectPresenter<BluetoothVolume.State>(),
) =
    BlueMoonTheme {
        Surface {
            bluetoothControllerPresenter.present()
            bluetoothVolumePresenter.present()

            Background()
            BlueMoonScaffold {
                DeviceSelector()

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
private fun UiEntryPointPreview() =
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
        overrides = {
            presenter<BlueMoonScaffold.State> {
                BlueMoonScaffold.State.Normal(
                    sheetContent = mutableStateOf(Touchpad),
                    scaffoldState = rememberBottomSheetScaffoldState(
                        bottomSheetState = rememberStandardBottomSheetState(initialValue = Expanded)
                    )
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
        overrides = {
            presenter<BlueMoonScaffold.State> {
                BlueMoonScaffold.State.Normal(
                    sheetContent = remember { mutableStateOf(Keyboard) },
                    scaffoldState = rememberBottomSheetScaffoldState(
                        bottomSheetState = rememberStandardBottomSheetState(initialValue = Expanded)
                    )
                )
            }
        }
    ) {
        UiEntryPoint()
    }
