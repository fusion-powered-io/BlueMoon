package io.fusionpowered.bluemoon.domain.volume.application

import io.fusionpowered.bluemoon.domain.volume.VolumeClient
import io.fusionpowered.bluemoon.domain.volume.model.VolumeState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.annotation.Single

@Single
actual class VolumeService : VolumeClient {

    override val volumeStateFlow: StateFlow<VolumeState>
        field = MutableStateFlow(VolumeState())

}