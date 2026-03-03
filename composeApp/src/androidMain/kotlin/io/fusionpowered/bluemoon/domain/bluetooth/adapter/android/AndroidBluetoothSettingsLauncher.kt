package io.fusionpowered.bluemoon.domain.bluetooth.adapter.android

import android.content.Context
import android.content.Intent
import android.provider.Settings
import io.fusionpowered.bluemoon.domain.bluetooth.port.BluetoothSettingsLauncher
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent

@Single
class AndroidBluetoothSettingsLauncher(
    private val applicationContext: Context,
) : BluetoothSettingsLauncher, KoinComponent {

    override fun launch() {
        Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            .apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            .let { applicationContext.startActivity(it) }
    }

}