package com.gu.recipe.core.graphql.config

import com.gu.recipe.core.graphql.provider.DefaultFeastGraphQlServerUrlProvider
import com.gu.recipe.core.graphql.provider.GraphQlServerUrlProvider

data class GraphQlConfig(
    val baseUrl: String,
    val serverUrlProvider: GraphQlServerUrlProvider = DefaultFeastGraphQlServerUrlProvider,
) {

    val serverUrl: String
        get() = serverUrlProvider.serverUrl(baseUrl)
}