package com.gu.recipe.core.graphql

import com.apollographql.apollo.ApolloClient
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
}

