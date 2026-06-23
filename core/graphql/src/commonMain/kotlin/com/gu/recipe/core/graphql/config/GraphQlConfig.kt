package com.gu.recipe.core.graphql.config

import com.gu.recipe.core.graphql.provider.DefaultFeastGraphQlServerUrlProvider
import com.gu.recipe.core.graphql.provider.GraphQlServerUrlProvider
import com.gu.recipe.core.networking.NetworkConfig

data class GraphQlConfig(
    val networkConfig: NetworkConfig,
    val serverUrlProvider: GraphQlServerUrlProvider = DefaultFeastGraphQlServerUrlProvider,
) {
    val serverUrl: String
        get() = serverUrlProvider.serverUrl(networkConfig)
}