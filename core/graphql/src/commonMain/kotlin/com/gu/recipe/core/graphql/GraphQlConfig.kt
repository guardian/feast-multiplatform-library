package com.gu.recipe.core.graphql

import com.gu.recipe.core.networking.NetworkConfig

data class GraphQlConfig(
    val networkConfig: NetworkConfig,
    val serverUrlProvider: GraphQlServerUrlProvider = DefaultFeastGraphQlServerUrlProvider,
) {
    val serverUrl: String
        get() = serverUrlProvider.serverUrl(networkConfig)
}

