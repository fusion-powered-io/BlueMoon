package io.fusionpowered.bluemoon.presentation.component.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.Icon
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import compose.icons.TablerIcons
import compose.icons.tablericons.Keyboard
import compose.icons.tablericons.MinusVertical
import compose.icons.tablericons.Mouse
import io.fusionpowered.bluemoon.bootstrap.KoinPresenter
import io.fusionpowered.bluemoon.bootstrap.injectPresenter
import io.fusionpowered.bluemoon.domain.bluetooth.BluetoothManager
import io.fusionpowered.bluemoon.domain.bluetooth.model.ConnectionState
import io.fusionpowered.bluemoon.presentation.component.section.BottomSheet
import io.fusionpowered.bluemoon.presentation.component.section.TopBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.Qualifier

object HomeLayout {

    @Qualifier(State::class)
    @KoinViewModel
    class Presenter(
        private val bluetoothManager: BluetoothManager,
    ) : KoinPresenter<State>() {

        @Composable
        override fun present(): State {
            val connected by bluetoothManager.connectionStateFlow.collectAsStateWithLifecycle(ConnectionState.Disconnected)
            val sheetContent = remember { mutableStateOf(State.SheetContent.Touchpad) }
            val scaffoldState = rememberBottomSheetScaffoldState(
                bottomSheetState = rememberStandardBottomSheetState(
                    skipHiddenState = false
                )
            )
            var blackOutState by remember { mutableStateOf(false) }


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

            return when {
                blackOutState -> State.BlackedOut(
                    onBlackoutClick = { blackOutState = false }
                )

                else -> State.Normal(
                    onPairClick = { bluetoothManager.launchBluetoothSettings() },
                    onBlackoutClick = { blackOutState = true },
                    sheetContent = sheetContent,
                    scaffoldState = scaffoldState
                )
            }
        }

    }

    sealed interface State {

        data class BlackedOut(
            val onBlackoutClick: () -> Unit = {},
        ) : State

        data class Normal(
            val onPairClick: () -> Unit = {},
            val onBlackoutClick: () -> Unit = {},
            val sheetContent: MutableState<SheetContent>,
            val scaffoldState: BottomSheetScaffoldState,
        ) : State

        enum class SheetContent {
            Touchpad,
            Keyboard,
        }
    }

    @Composable
    operator fun invoke(
        modifier: Modifier = Modifier,
        presenter: KoinPresenter<State> = injectPresenter<State>(),
        content: @Composable () -> Unit,
    ) {
        when (val state = presenter.present()) {
            is State.BlackedOut -> {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    onClick = state.onBlackoutClick,
                    color = Color.Black,
                ) {

                }
            }

            is State.Normal -> {
                BottomSheetScaffold(
                    modifier = modifier,
                    scaffoldState = state.scaffoldState,
                    sheetShape = RectangleShape,
                    topBar = {
                        TopBar(
                            onPairClick = state.onPairClick,
                            onBlackoutClick = state.onBlackoutClick,
                        )
                    },
                    sheetDragHandle = {
                        SheetDragHandle(
                            sheetContent = state.sheetContent,
                            sheetState = state.scaffoldState.bottomSheetState
                        )
                    },
                    sheetContent = {
                        BottomSheet(
                            sheetContent = state.sheetContent
                        )
                    },
                    sheetContainerColor = Color.Transparent,
                    sheetSwipeEnabled = true,
                    containerColor = Color.Transparent
                ) {
                    content()
                }
            }
        }
    }


    @Composable
    private fun SheetDragHandle(
        modifier: Modifier = Modifier.Companion,
        sheetContent: MutableState<State.SheetContent>,
        sheetState: SheetState,
        coroutineScope: CoroutineScope = rememberCoroutineScope(),
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(
                    Brush.verticalGradient(
                        0.0f to Color(0xFF6C7E9E),
                        1.0f to Color(0xFF9CB2D7),
                    )
                )
                .border(0.5.dp, Color.White.copy(alpha = 0.12f), androidx.compose.foundation.shape.RoundedCornerShape(2.dp)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            DragHandleIcon(
                icon = TablerIcons.Mouse,
                onClick = {
                    coroutineScope.launch {
                        if (sheetState.targetValue != SheetValue.Expanded) {
                            sheetState.expand()
                        }
                    }.invokeOnCompletion {
                        sheetContent.value = State.SheetContent.Touchpad
                    }
                }
            )
            DragHandleIcon(
                icon = TablerIcons.MinusVertical,
                onClick = {
                    coroutineScope.launch {
                        when (sheetState.targetValue) {
                            SheetValue.Expanded -> sheetState.partialExpand()
                            else -> sheetState.expand()
                        }
                    }
                }
            )
            DragHandleIcon(
                icon = TablerIcons.Keyboard,
                onClick = {
                    coroutineScope.launch {
                        if (sheetState.targetValue != SheetValue.Expanded) {
                            sheetState.expand()
                        }
                    }.invokeOnCompletion {
                        sheetContent.value = State.SheetContent.Keyboard
                    }
                }
            )
        }
    }

    @Composable
    private fun DragHandleIcon(
        modifier: Modifier = Modifier.Companion,
        icon: ImageVector,
        onClick: () -> Unit,
    ) {
        Surface(
            modifier = Modifier.wrapContentSize(),
            color = Color.Transparent,
            onClick = onClick
        ) {
            Icon(
                imageVector = icon,
                modifier = modifier.size(36.dp),
                tint = Color.DarkGray,
                contentDescription = "Keyboard",
            )
        }
    }



}