package io.fusionpowered.bluemoon.domain.bluetooth.application

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.PermissionChecker.checkSelfPermission
import io.fusionpowered.bluemoon.MainApplication
import io.fusionpowered.bluemoon.domain.bluetooth.BluetoothConnectionProvider
import io.fusionpowered.bluemoon.presentation.MainActivity
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.logger.Logger

@RequiresApi(Build.VERSION_CODES.S)
@SuppressLint("MissingPermission")
@Single
actual class BluetoothService actual constructor() : KoinComponent, BluetoothConnectionProvider {

    private val applicationContext: Context by inject()
    private val logger: Logger by inject()
    private val bluetoothManager: BluetoothManager = applicationContext.getSystemService(BluetoothManager::class.java)
    private val bluetoothAdapter: BluetoothAdapter = checkNotNull(bluetoothManager.adapter)

    val pairedDevices: Set<BluetoothDevice> = bluetoothAdapter.bondedDevices

    override fun send(key: String) {
        logger.info(key)
    }

}