package io.fusionpowered.bluemoon.domain.bluetooth.application

import io.fusionpowered.bluemoon.domain.bluetooth.BluetoothSettings
import org.koin.core.annotation.Single

@Single
expect class BluetoothSettingsService() : BluetoothSettings