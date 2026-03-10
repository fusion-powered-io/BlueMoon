package io.fusionpowered.bluemoon.domain.bluetooth

import io.fusionpowered.bluemoon.domain.bluetooth.port.BluetoothClient
import io.fusionpowered.bluemoon.domain.bluetooth.port.BluetoothSettingsLauncher
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
@ComponentScan
expect object BluetoothModule {

    @Single
    fun provideBluetoothClient(): BluetoothClient

    @Single
    fun provideBluetoothSettingsLauncher(): BluetoothSettingsLauncher

}