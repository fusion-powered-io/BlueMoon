package io.fusionpowered.bluemoon.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DragHandleSizes
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDragHandle
import androidx.compose.material3.VerticalDragHandleDefaults.colors
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import compose.icons.TablerIcons
import compose.icons.tablericons.Keyboard
import compose.icons.tablericons.Mouse
import io.fusionpowered.bluemoon.domain.bluetooth.BluetoothClient
import io.fusionpowered.bluemoon.domain.bluetooth.model.ConnectionState
import io.fusionpowered.bluemoon.presentation.components.BlueMoonScaffold.State.SheetContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

object BlueMoonScaffold {

    @Composable
    fun present(
        bluetoothClient: BluetoothClient = koinInject(),
    ): State {
        val connected by bluetoothClient.connectionStateFlow.collectAsStateWithLifecycle(ConnectionState.Disconnected)
        val sheetContent = remember { mutableStateOf(SheetContent.Touchpad) }
        val scaffoldState = rememberBottomSheetScaffoldState(
            bottomSheetState = rememberStandardBottomSheetState(
                skipHiddenState = false
            )
        )

        LaunchedEffect(connected) {
            when (connected) {
                is ConnectionState.Connected -> scaffoldState.bottomSheetState.show()
                else -> scaffoldState.bottomSheetState.hide()
            }
        }

        LaunchedEffect(scaffoldState.bottomSheetState.targetValue) {
            if (scaffoldState.bottomSheetState.targetValue == SheetValue.Hidden && connected is ConnectionState.Connected) {
                scaffoldState.bottomSheetState.partialExpand()
            }
        }

        return State(
            sheetContent = sheetContent,
            scaffoldState = scaffoldState
        )
    }

    data class State(
        val sheetContent: MutableState<SheetContent>,
        val scaffoldState: BottomSheetScaffoldState,
    ) {

        enum class SheetContent {
            Touchpad,
            Keyboard,
        }
    }

    @Composable
    operator fun invoke(
        modifier: Modifier = Modifier,
        presenter: @Composable () -> State = ::present,
        touchpadPresenter: @Composable () -> BluetoothTouchpad.State = { BluetoothTouchpad.present() },
        keyboardPresenter: @Composable () -> BluetoothKeyboard.State = { BluetoothKeyboard.present() },
        content: @Composable () -> Unit,
    ) {
        val state = presenter()
        BottomSheetScaffold(
            modifier = modifier,
            topBar = { TopBar() },
            scaffoldState = state.scaffoldState,
            sheetDragHandle = {
                SheetDragHandle(
                    sheetContent = state.sheetContent,
                    sheetState = state.scaffoldState.bottomSheetState
                )
            },
            sheetContent = { SheetContent(
                sheetContent = state.sheetContent,
                touchpadPresenter = touchpadPresenter,
                keyboardPresenter = keyboardPresenter
            ) },
            sheetContainerColor = Color.White,
            sheetSwipeEnabled = true
        ) {
            content()
        }
    }

    @Composable
    private fun TopBar(
        modifier: Modifier = Modifier,
    ) {
        CenterAlignedTopAppBar(
            modifier = modifier
                .shadow(elevation = 8.dp)
                .background(Color.White),
            title = {
                Text(
                    text = "Bluetooth Devices",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    )
                )
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.Transparent // Allows the parent Surface color to show
            )
        )
    }

    @Composable
    private fun SheetDragHandle(
        modifier: Modifier = Modifier,
        sheetContent: MutableState<SheetContent>,
        sheetState: SheetState,
        coroutineScope: CoroutineScope = rememberCoroutineScope(),
    ) {
        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = CenterVertically,
            horizontalArrangement = spacedBy(80.dp, Alignment.CenterHorizontally)
        ) {
            Surface(
                modifier = Modifier.wrapContentSize(),
                color = Color.Transparent,
                onClick = {
                    coroutineScope.launch {
                        if (sheetState.targetValue != SheetValue.Expanded) {
                            sheetState.expand()
                        }
                    }.invokeOnCompletion {
                        sheetContent.value = SheetContent.Touchpad
                    }
                }
            ) {
                Icon(
                    imageVector = TablerIcons.Mouse,
                    modifier = Modifier
                        .size(32.dp),
                    tint = Color.DarkGray,
                    contentDescription = "Trackpad",
                )
            }
            Surface(
                modifier = Modifier.wrapContentSize(),
                color = Color.Transparent,
                onClick = {
                    coroutineScope.launch {
                        when (sheetState.targetValue) {
                            SheetValue.Expanded -> sheetState.partialExpand()
                            else -> sheetState.expand()
                        }
                    }
                }
            ) {
                VerticalDragHandle(
                    sizes = DragHandleSizes(
                        size = DpSize(3.dp, 28.dp),
                        pressedSize = DpSize(3.dp, 28.dp),
                        draggedSize = DpSize(3.dp, 28.dp),
                    ),
                    colors = colors(color = Color.DarkGray)
                )
            }
            Surface(
                modifier = Modifier.wrapContentSize(),
                color = Color.Transparent,
                onClick = {
                    coroutineScope.launch {
                        if (sheetState.targetValue != SheetValue.Expanded) {
                            sheetState.expand()
                        }
                    }.invokeOnCompletion {
                        sheetContent.value = SheetContent.Keyboard
                    }
                }
            ) {
                Icon(
                    imageVector = TablerIcons.Keyboard,
                    modifier = Modifier
                        .size(32.dp),
                    tint = Color.DarkGray,
                    contentDescription = "Keyboard",
                )
            }
        }
    }

    @Composable
    private fun SheetContent(
        modifier: Modifier = Modifier,
        sheetContent: MutableState<SheetContent>,
        touchpadPresenter: @Composable () -> BluetoothTouchpad.State = { BluetoothTouchpad.present() },
        keyboardPresenter: @Composable () -> BluetoothKeyboard.State = { BluetoothKeyboard.present() },
    ) {
        AnimatedContent(
            modifier = modifier,
            targetState = sheetContent.value,
            label = "animated content"
        ) { target ->
            when (target) {
                SheetContent.Touchpad -> BluetoothTouchpad(
                    presenter = touchpadPresenter
                )

                SheetContent.Keyboard -> BluetoothKeyboard(
                    presenter = keyboardPresenter
                )
            }
        }
    }

}