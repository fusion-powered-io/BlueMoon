package io.fusionpowered.bluemoon.domain.bluetooth

import io.fusionpowered.bluemoon.domain.bluetooth.model.PairedDevice
import kotlinx.coroutines.flow.Flow

interface BluetoothConnectionProvider {

    val pairedDevicesFlow: Flow<List<PairedDevice>>

    fun send(key: String)

}