package io.fusionpowered.bluemoon.presentation.component.widget

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import compose.icons.TablerIcons
import compose.icons.tablericons.CirclePlus

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