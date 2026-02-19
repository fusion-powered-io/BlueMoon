package io.fusionpowered.bluemoon.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.fusionpowered.bluemoon.bootstrap.KoinPresenter
import io.fusionpowered.bluemoon.bootstrap.injectPresenter
import io.fusionpowered.bluemoon.domain.bluetooth.BluetoothClient
import io.fusionpowered.bluemoon.domain.bluetooth.model.ConnectionState
import io.fusionpowered.bluemoon.domain.keyboard.model.KeyboardState
import io.fusionpowered.bluemoon.domain.keyboard.model.KeyboardState.Key
import io.fusionpowered.bluemoon.presentation.components.BluetoothKeyboard.State.Connected.KeyInfo
import io.fusionpowered.bluemoon.presentation.preview.PreviewApplication
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Qualifier


object BluetoothKeyboard {

    @Qualifier(State::class)
    @Factory
    class Presenter(
        private val bluetoothClient: BluetoothClient,
    ) : KoinPresenter<State> {

        @Composable
        override fun present(): State {
            val connectionState by bluetoothClient.connectionStateFlow.collectAsStateWithLifecycle()
            val haptic = LocalHapticFeedback.current

            return when (val conn = connectionState) {
                is ConnectionState.Connected -> {
                    var activeModifiers by remember { mutableStateOf(setOf<Key.Modifier>()) }
                    var isCapsLocked by remember { mutableStateOf(false) }

                    // This strike approach currently limits me to one key press at a time.
                    // The HID can support up to 6 keys, so this could be improved.
                    fun strike(key: Key) {
                        haptic.performHapticFeedback(HapticFeedbackType.KeyboardTap)
                        bluetoothClient.send(conn.device, KeyboardState(activeModifiers, setOf(key)))
                        bluetoothClient.send(conn.device, KeyboardState(activeModifiers, emptySet()))
                    }

                    State.Connected(
                        getDisplayLabel = { info ->
                            val isShifted =
                                activeModifiers.any { it == Key.Modifier.LeftShift || it == Key.Modifier.RightShift }
                            when {
                                isShifted && info.shiftLabel != null -> info.shiftLabel
                                (isShifted || isCapsLocked) && info.key is Key.Letter -> info.label.uppercase()
                                else -> info.label.lowercase()
                            }
                        },
                        isHighlighted = { info ->
                            when (val k = info.key) {
                                is Key.Modifier -> activeModifiers.contains(k)
                                is Key.Function -> if (k == Key.Function.CapsLock) isCapsLocked else false
                                else -> false
                            }
                        },
                        onKeyClick = { info ->
                            when (val clickedKey = info.key) {
                                is Key.Modifier -> {
                                    if (clickedKey in activeModifiers) {
                                        haptic.performHapticFeedback(HapticFeedbackType.ToggleOff)
                                        activeModifiers = activeModifiers - clickedKey
                                    } else {
                                        haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                                        activeModifiers = activeModifiers + clickedKey
                                    }
                                }

                                is Key.Function -> {
                                    if (clickedKey == Key.Function.CapsLock) {
                                        isCapsLocked = !isCapsLocked
                                        strike(Key.Function.CapsLock)
                                    } else {
                                        strike(clickedKey)
                                    }
                                }

                                else -> {
                                    strike(clickedKey)
                                    activeModifiers = activeModifiers - Key.Modifier.LeftShift - Key.Modifier.RightShift
                                }
                            }
                        }
                    )
                }

                else -> State.Disconnected
            }
        }
    }

    sealed interface State {

        data object Disconnected : State

        data class Connected(
            val getDisplayLabel: (KeyInfo) -> String,
            val isHighlighted: (KeyInfo) -> Boolean,
            val onKeyClick: (KeyInfo) -> Unit = {},
        ) : State {

            data class KeyInfo(
                val label: String,
                val shiftLabel: String? = null,
                val key: Key,
                val weight: Float = 1f,
                val isAccent: Boolean = false,
            )

        }
    }

    @Composable
    operator fun invoke(
        modifier: Modifier = Modifier,
        presenter: KoinPresenter<State> = injectPresenter<State>(),
    ) {
        Box(modifier = modifier.fillMaxWidth()) {
            when (val state = presenter.present()) {
                is State.Disconnected -> {
                    Text(
                        text = "Keyboard Disconnected",
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.Gray
                    )
                }

                is State.Connected -> {
                    Column(
                        modifier = Modifier.padding(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        remember {
                            listOf(
                                listOf(
                                    KeyInfo("Esc", key = Key.Function.Escape, isAccent = true),
                                    KeyInfo("`", "~", Key.Symbol.Grave),
                                    KeyInfo("1", "!", Key.Number.Key1),
                                    KeyInfo("2", "@", Key.Number.Key2),
                                    KeyInfo("3", "#", Key.Number.Key3),
                                    KeyInfo("4", "$", Key.Number.Key4),
                                    KeyInfo("5", "%", Key.Number.Key5),
                                    KeyInfo("6", "^", Key.Number.Key6),
                                    KeyInfo("7", "&", Key.Number.Key7),
                                    KeyInfo("8", "*", Key.Number.Key8),
                                    KeyInfo("9", "(", Key.Number.Key9),
                                    KeyInfo("0", ")", Key.Number.Key0),
                                    KeyInfo("-", "_", Key.Symbol.Minus),
                                    KeyInfo("=", "+", Key.Symbol.Equal),
                                    KeyInfo("⌫", key = Key.Function.Backspace, weight = 1.5f, isAccent = true)
                                ),
                                listOf(
                                    KeyInfo("⇥", key = Key.Function.Tab, weight = 1.2f, isAccent = true),
                                    KeyInfo("Q", key = Key.Letter.Q),
                                    KeyInfo("W", key = Key.Letter.W),
                                    KeyInfo("E", key = Key.Letter.E),
                                    KeyInfo("R", key = Key.Letter.R),
                                    KeyInfo("T", key = Key.Letter.T),
                                    KeyInfo("Y", key = Key.Letter.Y),
                                    KeyInfo("U", key = Key.Letter.U),
                                    KeyInfo("I", key = Key.Letter.I),
                                    KeyInfo("O", key = Key.Letter.O),
                                    KeyInfo("P", key = Key.Letter.P),
                                    KeyInfo("[", "{", Key.Symbol.LeftBrace),
                                    KeyInfo("]", "}", Key.Symbol.RightBrace)
                                ),
                                listOf(
                                    KeyInfo("Caps", key = Key.Function.CapsLock, weight = 1.5f, isAccent = true),
                                    KeyInfo("A", key = Key.Letter.A),
                                    KeyInfo("S", key = Key.Letter.S),
                                    KeyInfo("D", key = Key.Letter.D),
                                    KeyInfo("F", key = Key.Letter.F),
                                    KeyInfo("G", key = Key.Letter.G),
                                    KeyInfo("H", key = Key.Letter.H),
                                    KeyInfo("J", key = Key.Letter.J),
                                    KeyInfo("K", key = Key.Letter.K),
                                    KeyInfo("L", key = Key.Letter.L),
                                    KeyInfo(";", ":", Key.Symbol.Semicolon),
                                    KeyInfo("'", "\"", Key.Symbol.Apostrophe),
                                    KeyInfo("⏎", key = Key.Function.Enter, weight = 1.8f, isAccent = true)
                                ),
                                listOf(
                                    KeyInfo("⇧", key = Key.Modifier.LeftShift, weight = 1.8f, isAccent = true),
                                    KeyInfo("Z", key = Key.Letter.Z),
                                    KeyInfo("X", key = Key.Letter.X),
                                    KeyInfo("C", key = Key.Letter.C),
                                    KeyInfo("V", key = Key.Letter.V),
                                    KeyInfo("B", key = Key.Letter.B),
                                    KeyInfo("N", key = Key.Letter.N),
                                    KeyInfo("M", key = Key.Letter.M),
                                    KeyInfo(",", "<", Key.Symbol.Comma),
                                    KeyInfo(".", ">", Key.Symbol.Dot),
                                    KeyInfo("/", "?", Key.Symbol.Slash),
                                    KeyInfo("↑", key = Key.Function.Up, isAccent = true, weight = 1.15f)
                                ),
                                listOf(
                                    KeyInfo("Ctrl", key = Key.Modifier.LeftControl, weight = 1.2f, isAccent = true),
                                    KeyInfo("Meta", key = Key.Modifier.LeftMeta, weight = 1.2f, isAccent = true),
                                    KeyInfo("Alt", key = Key.Modifier.LeftAlt, weight = 1.2f, isAccent = true),
                                    KeyInfo("Space", key = Key.Function.Space, weight = 3.5f),
                                    KeyInfo("←", key = Key.Function.Left, isAccent = true, weight = 0.8f),
                                    KeyInfo("→", key = Key.Function.Right, isAccent = true, weight = 0.8f),
                                    KeyInfo("↓", key = Key.Function.Down, isAccent = true, weight = 0.8f),
                                )
                            )
                        }.forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                row.forEach { info ->
                                    KeyboardKey(
                                        label = state.getDisplayLabel(info),
                                        weight = info.weight,
                                        isAccent = info.isAccent,
                                        isHighlighted = state.isHighlighted(info),
                                        onClick = { state.onKeyClick(info) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun RowScope.KeyboardKey(
        label: String,
        weight: Float,
        isAccent: Boolean,
        isHighlighted: Boolean,
        onClick: () -> Unit,
    ) {
        val themeColor = Color(0xFF4A90E2) // PS2 Blue
        val highlightColor = Color(0xFF00FFCC) // PS2 Cyan/Green

        // Dynamic styling based on state
        val borderColor = when {
            isHighlighted -> highlightColor.copy(alpha = 0.8f)
            isAccent -> themeColor.copy(alpha = 0.5f)
            else -> Color.White.copy(alpha = 0.12f)
        }

        val backgroundColor = when {
            isHighlighted -> highlightColor.copy(alpha = 0.2f)
            isAccent -> Color.White.copy(alpha = 0.08f)
            else -> Color.White.copy(alpha = 0.04f)
        }

        Box(
            modifier = Modifier
                .weight(weight)
                .height(48.dp)
                .padding(2.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(backgroundColor, Color.Transparent)
                    )
                )
                .border(
                    width = 0.5.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(2.dp)
                )
                .clickable(onClick = onClick)
                .drawBehind {
                    drawLine(
                        color = Color.White.copy(alpha = 0.1f),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        strokeWidth = 1.dp.toPx()
                    )
                    if (isHighlighted) {
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(highlightColor.copy(alpha = 0.15f), Color.Transparent),
                                center = center,
                                radius = size.maxDimension
                            )
                        )
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label.uppercase(),
                color = if (isHighlighted) highlightColor else Color.White.copy(alpha = 0.8f),
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 0.5.sp
                )
            )
        }
    }
}

@Preview
@Composable
fun BluetoothKeyboardPreview() =
    PreviewApplication {
        BluetoothKeyboard()
    }

