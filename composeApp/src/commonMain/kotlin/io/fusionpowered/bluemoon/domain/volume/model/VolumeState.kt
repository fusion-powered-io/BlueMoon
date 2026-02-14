package io.fusionpowered.bluemoon.domain.volume.model

import kotlin.time.Clock

data class VolumeState(
    val id: Long = Clock.System.now().toEpochMilliseconds(),
    val up: Boolean = false,
    val down: Boolean = false,
)