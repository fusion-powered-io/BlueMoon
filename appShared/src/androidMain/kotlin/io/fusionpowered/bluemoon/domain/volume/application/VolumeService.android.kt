package io.fusionpowered.bluemoon.domain.volume.application

import android.view.KeyEvent
import android.view.KeyEvent.*
import io.fusionpowered.bluemoon.domain.volume.VolumeClient
import io.fusionpowered.bluemoon.domain.volume.model.VolumeState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import kotlin.time.Clock

@Single
actual class VolumeService actual constructor() : VolumeClient, KoinComponent {

    final override val volumeStateFlow: StateFlow<VolumeState>
        field = MutableStateFlow(VolumeState())


    fun handle(input: KeyEvent) {
        val isDown = input.action == ACTION_DOWN
        volumeStateFlow.update { volumeState ->
            when (input.keyCode) {
                KEYCODE_VOLUME_UP -> volumeState.copy(up = isDown)
                KEYCODE_VOLUME_DOWN -> volumeState.copy(down = isDown)
                else -> volumeState
            }.copy(id = Clock.System.now().toEpochMilliseconds())
        }
    }

}