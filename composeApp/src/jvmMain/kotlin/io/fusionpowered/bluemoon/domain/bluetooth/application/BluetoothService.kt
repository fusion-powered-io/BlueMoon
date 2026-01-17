package io.fusionpowered.bluemoon.domain.bluetooth.application

import io.fusionpowered.bluemoon.domain.bluetooth.BluetoothConnectionProvider
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

@Single
actual class BluetoothService actual constructor() : KoinComponent, BluetoothConnectionProvider {

    private val logger: Logger by inject()

    override val pairedDevicesFlow: Flow<List<PairedDevice>> =
        flow {
            while (true) {
                listOf(
                    PairedDevice(
                        name = "test device 1",
                        mac = "92:B1:B8:42:D1:85"
                    ),
                    PairedDevice(
                        name = "test device 2",
                        mac = "4b:57:71:1d:d4:96"
                    )
                )
                    .let { emit(it) }
                delay(1.seconds)
            }
        }.distinctUntilChanged()

    override fun send(key: String) {
        logger.info(key)
    }

}