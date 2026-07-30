package com.gu.recipe.api.di

import com.gu.recipe.api.repository.GraphQlRepositoryImpl
import com.gu.recipe.api.repository.GraphQLRepository
import com.gu.recipe.core.graphql.config.GraphQlConfig
import com.gu.recipe.core.graphql.di.graphQlModule
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Creates the Koin module for the Feast API layer.
 *
 * @param baseUrl Base URL used to configure GraphQL networking.
 * @param ioDispatcher Dispatcher used for GraphQL I/O work.
 * @return A configured Koin [Module] with GraphQL and repository bindings.
 */
fun feastApiModule(
    baseUrl: String,
    ioDispatcher: CoroutineDispatcher = Dispatchers.Default,
): Module = module {
    includes(graphQlModule(GraphQlConfig(baseUrl = baseUrl), ioDispatcher))
    single<GraphQLRepository> { GraphQlRepositoryImpl(get()) }
}