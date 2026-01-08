package io.fusionpowered.bluemoon.di

import org.koin.core.KoinApplication
import org.koin.core.context.GlobalContext.startKoin
import org.koin.ksp.generated.*

fun initKoin(platformSpecificInitialization: KoinApplication.() -> Unit = {}) {
    startKoin {
        platformSpecificInitialization()
        modules(AppModule().module)
    }
}