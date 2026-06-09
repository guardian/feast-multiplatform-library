package com.gu.recipe.core.graphql

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.annotations.ApolloExperimental
import kotlinx.coroutines.CoroutineDispatcher

class ApolloClientFactory(
    private val dispatcher: CoroutineDispatcher,
) {
    @OptIn(ApolloExperimental::class)
    fun create(config: GraphQlConfig): ApolloClient =
        ApolloClient.Builder()
            .serverUrl(config.serverUrl)
            .dispatcher(dispatcher)
            .failFastIfOffline(true)
            .build()
}

