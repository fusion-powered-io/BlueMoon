package io.fusionpowered.bluemoon.domain.volume.application

import io.fusionpowered.bluemoon.domain.volume.VolumeClient
import org.koin.core.annotation.Single

@Single
expect class VolumeService(): VolumeClient