package com.gu.recipe.core.graphql

import com.apollographql.apollo.ApolloClient

class ApolloClientFactory {
    fun create(config: GraphQlConfig): ApolloClient =
        ApolloClient.Builder()
            .serverUrl(config.serverUrl)
            .build()
}

