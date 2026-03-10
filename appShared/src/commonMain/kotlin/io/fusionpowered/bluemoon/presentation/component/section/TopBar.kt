package io.fusionpowered.bluemoon.presentation.component.section

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.fusionpowered.bluemoon.presentation.component.widget.BlackoutButton
import io.fusionpowered.bluemoon.presentation.component.widget.PairNewDeviceButton

@Composable
fun TopBar(
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