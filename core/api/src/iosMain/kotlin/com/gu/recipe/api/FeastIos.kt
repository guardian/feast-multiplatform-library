package com.gu.recipe.api

import com.gu.recipe.api.config.FeastApiConfig
import com.gu.recipe.api.di.iosFeastApiModule
import com.gu.recipe.api.model.FeastEnvironment
import com.gu.recipe.api.repository.GraphQLRepository
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.mp.KoinPlatform

object FeastIos {
    fun start(
        environment: FeastEnvironment,
    ) {
        start(
            FeastApiConfig(
                environment = environment,
            ),
        )
    }

    fun start(config: FeastApiConfig) {
        KoinPlatform.getKoinOrNull()?.let { stopKoin() }
        startKoin {
            modules(iosFeastApiModule(config))
        }
    }

    fun recipeRepository(): GraphQLRepository = KoinPlatform.getKoin().get()

    fun stop() {
        KoinPlatform.getKoinOrNull()?.let { stopKoin() }
    }
}


