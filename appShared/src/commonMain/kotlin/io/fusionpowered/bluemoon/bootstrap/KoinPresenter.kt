package io.fusionpowered.bluemoon.bootstrap

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import org.koin.compose.koinInject
import org.koin.core.definition.KoinDefinition
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.parameter.ParametersHolder
import org.koin.core.parameter.emptyParametersHolder
import org.koin.core.qualifier.named

abstract class KoinPresenter<T> : ViewModel() {

    @Composable
    abstract fun present(): T

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
    viewModel<KoinPresenter<T>>(named<T>()) { parameters ->
        object: KoinPresenter<T>() {
            @Composable
            override fun present() = stateCreator(parameters)
        }
    }