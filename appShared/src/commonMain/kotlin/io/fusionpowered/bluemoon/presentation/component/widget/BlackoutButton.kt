package io.fusionpowered.bluemoon.presentation.component.widget

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.Bulb

@Composable
fun BlackoutButton(
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