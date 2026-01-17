package io.fusionpowered.bluemoon.presentation.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import io.fusionpowered.bluemoon.presentation.views.selectconnection.SelectConnectionScreen
import org.koin.core.annotation.Single
import org.koin.core.logger.Logger

@Single
class Navigator(
    private val logger: Logger
) {

    val backStack: SnapshotStateList<Any> = mutableStateListOf(SelectConnectionScreen)

    fun navigateTo(destination: Any) {
        logger.debug("Navigating to ${destination::class.simpleName}")
        backStack.add(destination)
    }

    fun goBack() {
        backStack.removeLastOrNull()
            ?.let { destination -> logger.debug("Navigating back to ${destination::class.simpleName}") }
            ?: { logger.debug("Tried navigating back, but nowhere to go") }
    }

}