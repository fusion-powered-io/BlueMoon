package io.fusionpowered.bluemoon.presentation

import androidx.compose.material3.SheetValue.Expanded
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import io.fusionpowered.bluemoon.bootstrap.KoinApp
import io.fusionpowered.bluemoon.bootstrap.PreviewApplication
import io.fusionpowered.bluemoon.bootstrap.presenter
import io.fusionpowered.bluemoon.presentation.component.layout.HomeLayout
import io.fusionpowered.bluemoon.presentation.component.layout.HomeLayout.State.SheetContent.Keyboard
import io.fusionpowered.bluemoon.presentation.component.layout.HomeLayout.State.SheetContent.Touchpad
import io.fusionpowered.bluemoon.presentation.page.Home
import io.fusionpowered.bluemoon.presentation.theme.BlueMoonTheme
import org.koin.compose.KoinApplication
import org.koin.dsl.koinConfiguration
import org.koin.ksp.generated.configurationModules


@Composable
fun UiEntryPoint(
    platformInitialization: @Composable () -> Unit = { },
) {
    KoinApplication(
        koinConfiguration {
            modules(KoinApp.configurationModules)
        }
    ) {
        platformInitialization()
        BlueMoonTheme {
            Surface {
                Home()
            }
        }
    }
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
            presenter<HomeLayout.State> {
                HomeLayout.State.Normal(
                    sheetContent = remember { mutableStateOf(Touchpad) },
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
            presenter<HomeLayout.State> {
                HomeLayout.State.Normal(
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
