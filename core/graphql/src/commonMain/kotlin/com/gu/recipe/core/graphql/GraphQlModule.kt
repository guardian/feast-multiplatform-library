package com.gu.recipe.core.graphql

import com.apollographql.apollo.ApolloClient
import org.koin.core.module.Module
import org.koin.dsl.module

fun graphQlModule(config: GraphQlConfig): Module = module {
    single { config }
    single<GraphQlServerUrlProvider> { get<GraphQlConfig>().serverUrlProvider }
    single { ApolloClientFactory() }
    single<ApolloClient> { get<ApolloClientFactory>().create(get()) }
    single { FeastGraphQlClient(get()) }
}

