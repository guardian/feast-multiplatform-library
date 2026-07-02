package com.gu.recipe.api

import com.gu.recipe.api.config.FeastApiConfig
import com.gu.recipe.api.di.iosFeastApiModule
import com.gu.recipe.api.repository.RecipeRepository
import com.gu.recipe.core.networking.FeastNetworkApiEndpoint
import com.gu.recipe.core.networking.NetworkConfig
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.mp.KoinPlatform

object FeastIos {
    fun start(
        endpoint: FeastNetworkApiEndpoint,
        serverUrl: String? = null,
    ) {
        start(
            FeastApiConfig(
                networkConfig = NetworkConfig(endpoint = endpoint),
                serverUrl = serverUrl,
            ),
        )
    }

    fun start(config: FeastApiConfig) {
        KoinPlatform.getKoinOrNull()?.let { stopKoin() }
        startKoin {
            modules(iosFeastApiModule(config))
        }
    }

    fun recipeRepository(): RecipeRepository = KoinPlatform.getKoin().get()

    fun stop() {
        KoinPlatform.getKoinOrNull()?.let { stopKoin() }
    }
}


