package io.fusionpowered.bluemoon.adapter.bluetooth

import io.fusionpowered.bluemoon.port.InputSender
import org.koin.core.annotation.Single

@Single
expect class BluetoothInputSender() : InputSender