package io.fusionpowered.bluemoon.bootstrap

import androidx.compose.runtime.Composable
import org.koin.compose.koinInject
import org.koin.core.definition.KoinDefinition
import org.koin.core.module.Module
import org.koin.core.parameter.ParametersHolder
import org.koin.core.parameter.emptyParametersHolder
import org.koin.core.qualifier.named

fun interface KoinPresenter<T> {

    @Composable
    fun present(): T

}

@Composable
inline fun <reified T> injectPresenter(
    parametersProvider: () -> ParametersHolder = { emptyParametersHolder() }
): KoinPresenter<T> =
    koinInject<KoinPresenter<T>>(
        qualifier = named<T>(),
        parametersHolder = parametersProvider()
    )

inline fun <reified T> Module.presenter(
    crossinline stateCreator: @Composable (ParametersHolder) -> T
): KoinDefinition<KoinPresenter<T>> =
    factory<KoinPresenter<T>>(named<T>()) { parameters ->
        KoinPresenter {
            stateCreator(parameters)
        }
    }