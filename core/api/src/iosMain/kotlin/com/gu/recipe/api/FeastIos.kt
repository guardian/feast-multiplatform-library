package com.gu.recipe.api

import com.gu.recipe.api.di.iosFeastApiModule
import com.gu.recipe.api.repository.GraphQLRepository
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.mp.KoinPlatform

object FeastIos {
    fun start(
        baseUrl: String,
    ) {
        KoinPlatform.getKoinOrNull()?.let { stopKoin() }
        startKoin {
            modules(iosFeastApiModule(baseUrl))
        }
    }

    fun recipeRepository(): GraphQLRepository = KoinPlatform.getKoin().get()

    fun stop() {
        KoinPlatform.getKoinOrNull()?.let { stopKoin() }
    }
}


