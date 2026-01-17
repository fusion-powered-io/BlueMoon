package io.fusionpowered.bluemoon.presentation.views.selectconnection

import io.fusionpowered.bluemoon.domain.bluetooth.model.PairedDevice
import kotlinx.serialization.Serializable

@Serializable
object SelectConnectionScreen {

    sealed interface State {

        data object NoPairedDevices : State

        data class ShowingPairedDevices(
            val pairedDevices: List<PairedDevice> = emptyList(),
            val onDeviceClicked: (pairedDevice: PairedDevice) -> Unit = {},
        ) : State

    }

}