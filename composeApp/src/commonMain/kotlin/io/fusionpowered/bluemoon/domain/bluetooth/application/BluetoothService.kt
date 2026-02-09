package io.fusionpowered.bluemoon.domain.bluetooth.application

import io.fusionpowered.bluemoon.domain.bluetooth.BluetoothClient
import org.koin.core.annotation.Single

@Single
expect class BluetoothService() : BluetoothClient