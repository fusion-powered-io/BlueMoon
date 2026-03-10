package io.fusionpowered.bluemoon.presentation.component.section

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import io.fusionpowered.bluemoon.presentation.component.layout.HomeLayout.State.SheetContent
import io.fusionpowered.bluemoon.presentation.component.widget.BluetoothKeyboard
import io.fusionpowered.bluemoon.presentation.component.widget.BluetoothTouchpad

@Composable
fun BottomSheet(
    modifier: Modifier = Modifier,
    sheetContent: MutableState<SheetContent>,
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