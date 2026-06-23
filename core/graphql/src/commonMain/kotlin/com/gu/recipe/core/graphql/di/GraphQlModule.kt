package com.gu.recipe.core.graphql.di

import com.apollographql.apollo.ApolloClient
import com.gu.recipe.core.graphql.client.ApolloClientFactory
import com.gu.recipe.core.graphql.client.FeastGraphQlClient
import com.gu.recipe.core.graphql.config.GraphQlConfig
import com.gu.recipe.core.graphql.provider.GraphQlServerUrlProvider
import com.gu.recipe.core.graphql.repository.ApolloRecipeGraphQlDataSource
import com.gu.recipe.core.graphql.repository.RecipeGraphQlDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

internal object GraphQlQualifiers {
    const val IoDispatcher = "graphQlIoDispatcher"
}

fun graphQlModule(
    config: GraphQlConfig,
    ioDispatcher: CoroutineDispatcher = Dispatchers.Default,
): Module = module {
    single { config }
    single<GraphQlServerUrlProvider> { get<GraphQlConfig>().serverUrlProvider }
    single<CoroutineDispatcher>(named(GraphQlQualifiers.IoDispatcher)) { ioDispatcher }
    single { ApolloClientFactory(get(named(GraphQlQualifiers.IoDispatcher))) }
    single<ApolloClient> { get<ApolloClientFactory>().create(get()) }
    single { FeastGraphQlClient(get()) }
    single<RecipeGraphQlDataSource> { ApolloRecipeGraphQlDataSource(get()) }
}

