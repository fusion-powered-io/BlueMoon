package io.fusionpowered.bluemoon.di

import io.fusionpowered.bluemoon.BT.AndroidBluetoothController
import io.fusionpowered.bluemoon.domain.BluetoothController
import io.fusionpowered.bluemoon.ui.BluetoothViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val bluetoothModule = module {
    single<BluetoothController> {
        AndroidBluetoothController(androidContext())
    }

    viewModel {
        BluetoothViewModel(get())
    }

}