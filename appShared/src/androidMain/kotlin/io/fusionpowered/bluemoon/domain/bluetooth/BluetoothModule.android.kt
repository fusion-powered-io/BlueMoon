package io.fusionpowered.bluemoon.domain.bluetooth

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import io.fusionpowered.bluemoon.domain.bluetooth.adapter.android.AndroidBluetoothSettingsLauncher
import io.fusionpowered.bluemoon.domain.bluetooth.adapter.foregroundservice.ForegroundServiceBluetoothClient
import io.fusionpowered.bluemoon.domain.bluetooth.port.BluetoothClient
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
    actual fun provideBluetoothClient(): BluetoothClient {
        val activity = getKoin().get<ComponentActivity>()
        val intent = Intent(activity, ForegroundServiceBluetoothClient::class.java)

        //This is a HACK to synchronously obtain the foreground service
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
            }
        }

        when (val binder = receiver.peekService(activity, intent)) {
            is ForegroundServiceBluetoothClient.LocalBinder -> return binder.getService()
            else -> throw IllegalStateException("ForegroundServiceBluetoothClient not started")
        }
    }

    @Single
    actual fun provideBluetoothSettingsLauncher(): BluetoothSettingsLauncher {
        return AndroidBluetoothSettingsLauncher()
    }

}
