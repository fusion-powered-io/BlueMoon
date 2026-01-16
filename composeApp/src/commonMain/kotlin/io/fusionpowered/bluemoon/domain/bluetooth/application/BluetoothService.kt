package io.fusionpowered.bluemoon.domain.bluetooth.application

import io.fusionpowered.bluemoon.domain.bluetooth.BluetoothConnectionProvider
import org.koin.core.annotation.Single

@Single
expect class BluetoothService() : BluetoothConnectionProvider