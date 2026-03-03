package io.fusionpowered.bluemoon.domain.bluetooth

import io.fusionpowered.bluemoon.domain.bluetooth.adapter.mock.MockBluetoothSettingsLauncher
import io.fusionpowered.bluemoon.domain.bluetooth.port.BluetoothSettingsLauncher
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
@ComponentScan
actual object BluetoothModule {

    @Single
    actual fun provideBluetoothSettingsLauncher(): BluetoothSettingsLauncher {
        return MockBluetoothSettingsLauncher()
    }

}