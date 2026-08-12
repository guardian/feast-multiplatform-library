package com.gu.recipe.api.di

import com.gu.recipe.api.repository.GraphQLRepository
import com.gu.recipe.api.repository.GraphQlRepositoryImpl
import com.gu.recipe.backend.graphql.config.GraphQlConfig
import com.gu.recipe.backend.graphql.di.graphQlModule
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Creates a Koin module for the Feast API with GraphQL support.
 *
 * @param baseUrl The base URL for GraphQL requests
 * @param ioDispatcher The coroutine dispatcher for IO operations (defaults to Dispatchers.IO)
 * @return A Koin Module configured with GraphQL repository
 */
fun androidFeastApiModule(
    baseUrl: String,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
): Module = module {
    includes(
        _root_ide_package_.com.gu.recipe.backend.graphql.di.graphQlModule(
            _root_ide_package_.com.gu.recipe.backend.graphql.config.GraphQlConfig(
                baseUrl = baseUrl
            ), ioDispatcher
        )
    )
    single<GraphQLRepository> { GraphQlRepositoryImpl(get()) }
}

