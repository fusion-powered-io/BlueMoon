package io.fusionpowered.bluemoon.domain.bluetooth

import android.content.Context
import io.fusionpowered.bluemoon.domain.bluetooth.adapter.android.AndroidBluetoothSettingsLauncher
import io.fusionpowered.bluemoon.domain.bluetooth.port.BluetoothSettingsLauncher
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import org.koin.java.KoinJavaComponent.getKoin

@Module
@Configuration
@ComponentScan
actual object BluetoothModule {

    @Single
    actual fun provideBluetoothSettingsLauncher(): BluetoothSettingsLauncher {
        val applicationContext = getKoin().get<Context>()
        return AndroidBluetoothSettingsLauncher(applicationContext)
    }


}
