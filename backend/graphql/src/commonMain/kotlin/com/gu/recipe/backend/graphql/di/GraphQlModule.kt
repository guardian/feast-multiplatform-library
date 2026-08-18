package com.gu.recipe.backend.graphql.di

import com.apollographql.apollo.ApolloClient
import com.gu.recipe.backend.graphql.client.ApolloClientFactory
import com.gu.recipe.backend.graphql.client.FeastGraphQlClient
import com.gu.recipe.backend.graphql.config.GraphQlConfig
import com.gu.recipe.backend.graphql.provider.GraphQlServerUrlProvider
import com.gu.recipe.backend.graphql.repository.ApolloRecipeGraphQlDataSource
import com.gu.recipe.backend.graphql.repository.RecipeGraphQlDataSource
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
    single<ApolloClient> {
        get<ApolloClientFactory>().create(
            config = get(),
        )
    }
    single { FeastGraphQlClient(get()) }
    single<RecipeGraphQlDataSource> { ApolloRecipeGraphQlDataSource(get()) }
}

