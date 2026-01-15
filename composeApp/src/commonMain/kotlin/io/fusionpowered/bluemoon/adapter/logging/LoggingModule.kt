package io.fusionpowered.bluemoon.adapter.logging

import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import org.koin.core.scope.Scope

@Module
@Configuration
object LoggingModule {

    @Single
    fun providesLogger(scope: Scope) =
        scope.logger

}