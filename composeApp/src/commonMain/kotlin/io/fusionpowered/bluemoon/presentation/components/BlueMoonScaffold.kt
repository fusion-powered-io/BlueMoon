package io.fusionpowered.bluemoon.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import compose.icons.TablerIcons
import compose.icons.tablericons.*
import io.fusionpowered.bluemoon.bootstrap.KoinPresenter
import io.fusionpowered.bluemoon.bootstrap.injectPresenter
import io.fusionpowered.bluemoon.domain.bluetooth.BluetoothClient
import io.fusionpowered.bluemoon.domain.bluetooth.BluetoothSettings
import io.fusionpowered.bluemoon.domain.bluetooth.model.ConnectionState
import io.fusionpowered.bluemoon.presentation.components.BlueMoonScaffold.State.SheetContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Qualifier

object BlueMoonScaffold {

    @Qualifier(State::class)
    @Factory
    class Presenter(
        private val bluetoothClient: BluetoothClient,
        private val bluetoothSettings: BluetoothSettings,
    ) : KoinPresenter<State> {

        @Composable
        override fun present(): State {
            val connected by bluetoothClient.connectionStateFlow.collectAsStateWithLifecycle(ConnectionState.Disconnected)
            val sheetContent = remember { mutableStateOf(SheetContent.Touchpad) }
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
                    onPairClick = { bluetoothSettings.launch() },
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
                        SheetContent(
                            sheetContent = state.sheetContent
                        )
                    },
                    // THEME OVERRIDES:
                    sheetContainerColor = Color.Transparent, // Makes the M3 surface invisible
                    sheetSwipeEnabled = true,
                    containerColor = Color.Transparent
                ) {
                    content()
                }
            }
        }
    }

    @Composable
    private fun TopBar(
        modifier: Modifier = Modifier,
        onPairClick: () -> Unit = {},
        onBlackoutClick: () -> Unit = {},
    ) {
        Row(
            modifier = modifier
                .background(
                    Brush.verticalGradient(
                        0.0f to Color(0xFF6C7E9E),
                        1.0f to Color(0xFF9CB2D7),
                    )
                )
                .border(0.5.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(2.dp))
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            PairNewDeviceButton(onClick = onPairClick)
            BlackoutButton(onClick = onBlackoutClick)
        }
    }

    @Composable
    fun PairNewDeviceButton(
        modifier: Modifier = Modifier,
        onClick: () -> Unit,
    ) {
        Surface(
            modifier = modifier.wrapContentSize(),
            color = Color.Transparent,
            onClick = onClick,
        ) {
            Row(
                modifier = Modifier.wrapContentSize()
                    .background(Color.Transparent),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.Transparent,
                    border = BorderStroke(2.dp, Color.DarkGray),
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = TablerIcons.CirclePlus,
                        contentDescription = null,
                        modifier = Modifier.padding(2.dp),
                        tint = Color.DarkGray
                    )
                }

                Spacer(Modifier.width(12.dp))

                Text(
                    text = "Pair",
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = Color.DarkGray
                    )
                )
            }
        }
    }

    @Composable
    private fun BlackoutButton(
        modifier: Modifier = Modifier,
        onClick: () -> Unit,
    ) {
        Surface(
            modifier = modifier.wrapContentSize(),
            onClick = onClick,
            color = Color.Transparent,
        ) {
            Icon(
                modifier = Modifier.size(36.dp),
                imageVector = TablerIcons.Bulb,
                contentDescription = null,
                tint = Color.DarkGray
            )
        }
    }


    @Composable
    private fun SheetDragHandle(
        modifier: Modifier = Modifier,
        sheetContent: MutableState<SheetContent>,
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
                .border(0.5.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(2.dp)),
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
                        sheetContent.value = SheetContent.Touchpad
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
                        sheetContent.value = SheetContent.Keyboard
                    }
                }
            )
        }
    }

    @Composable
    private fun DragHandleIcon(
        modifier: Modifier = Modifier,
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

    @Composable
    private fun SheetContent(
        modifier: Modifier = Modifier,
        sheetContent: MutableState<SheetContent>
    ) {
        AnimatedContent(
            modifier = modifier
                .background(
                    Brush.verticalGradient(
                        0.0f to Color(0xFF5C6A83),
                        1.0f to Color(0xFF181C2F),
                    )
                ),
            targetState = sheetContent.value,
            label = "animated content"
        ) { target ->
            when (target) {
                SheetContent.Touchpad -> BluetoothTouchpad()
                SheetContent.Keyboard -> BluetoothKeyboard()
            }
        }
    }

}