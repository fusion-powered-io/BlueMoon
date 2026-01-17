package io.fusionpowered.bluemoon.domain.bluetooth.application

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import io.fusionpowered.bluemoon.domain.bluetooth.BluetoothConnectionProvider
import io.fusionpowered.bluemoon.domain.bluetooth.mapper.toPairedDevice
import io.fusionpowered.bluemoon.domain.bluetooth.model.PairedDevice
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.logger.Logger
import kotlin.time.Duration.Companion.seconds

@RequiresApi(Build.VERSION_CODES.S)
@SuppressLint("MissingPermission")
@Single
actual class BluetoothService actual constructor() : KoinComponent, BluetoothConnectionProvider {

    private val applicationContext: Context by inject()
    private val logger: Logger by inject()
    private val bluetoothManager: BluetoothManager = applicationContext.getSystemService(BluetoothManager::class.java)
    private val bluetoothAdapter: BluetoothAdapter = checkNotNull(bluetoothManager.adapter)

    override val pairedDevicesFlow: Flow<List<PairedDevice>> =
        flow {
            while (true) {
                bluetoothAdapter.bondedDevices
                    .map { it.toPairedDevice() }
                    .let { emit(it) }
                delay(1.seconds)
            }
        }.distinctUntilChanged()

    override fun send(key: String) {
        logger.info(key)
    }

}