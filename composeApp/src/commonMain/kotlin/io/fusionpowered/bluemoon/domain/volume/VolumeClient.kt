package io.fusionpowered.bluemoon.domain.volume

import io.fusionpowered.bluemoon.domain.volume.model.VolumeState
import kotlinx.coroutines.flow.StateFlow

interface VolumeClient {

    val volumeStateFlow: StateFlow<VolumeState>

}