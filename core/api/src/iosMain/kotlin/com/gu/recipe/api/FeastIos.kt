package com.gu.recipe.api

import com.gu.recipe.api.di.iosFeastApiModule
import com.gu.recipe.api.repository.GraphQLRepository
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.mp.KoinPlatform

/**
 * iOS entry point for configuring and accessing Feast API dependencies.
 */
object FeastIos {
    /**
     * Starts the dependency container for iOS with the provided API base URL.
     *
     * Any existing Koin instance is stopped before a new one is created.
     *
     * @param baseUrl the base URL used to configure the API module
     */
    fun start(
        baseUrl: String,
    ) {
        KoinPlatform.getKoinOrNull()?.let { stopKoin() }
        startKoin {
            modules(iosFeastApiModule(baseUrl))
        }
    }

    /**
     * Returns the configured GraphQL repository instance.
     *
     * @return the resolved `GraphQLRepository`
     */
    fun recipeRepository(): GraphQLRepository = KoinPlatform.getKoin().get()

    /**
     * Stops the active Koin instance if one is running.
     */
    fun stop() {
        KoinPlatform.getKoinOrNull()?.let { stopKoin() }
    }
}

