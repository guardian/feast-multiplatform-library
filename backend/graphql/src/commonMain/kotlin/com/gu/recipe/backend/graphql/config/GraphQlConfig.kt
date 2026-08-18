package com.gu.recipe.backend.graphql.config

import com.gu.recipe.backend.graphql.provider.DefaultFeastGraphQlServerUrlProvider
import com.gu.recipe.backend.graphql.provider.GraphQlServerUrlProvider

data class GraphQlConfig(
    val baseUrl: String,
    val serverUrlProvider: GraphQlServerUrlProvider = DefaultFeastGraphQlServerUrlProvider,
) {

    val serverUrl: String
        get() = serverUrlProvider.serverUrl(baseUrl)
}