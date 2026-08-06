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
 * Creates the iOS API Koin module for Feast.
 *
 * @param baseUrl Base URL used to configure GraphQL.
 * @param ioDispatcher Coroutine dispatcher used by GraphQL dependencies (defaults to Dispatchers.Default).
 * @return Koin [Module] with GraphQL and repository bindings.
 */
fun iosFeastApiModule(
    baseUrl: String,
    ioDispatcher: CoroutineDispatcher = Dispatchers.Default,
): Module = module {
    includes(graphQlModule(GraphQlConfig(baseUrl = baseUrl), ioDispatcher))
    single<GraphQLRepository> { GraphQlRepositoryImpl(get()) }
}