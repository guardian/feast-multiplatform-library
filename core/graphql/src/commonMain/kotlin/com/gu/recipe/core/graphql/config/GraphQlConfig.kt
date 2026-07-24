package com.gu.recipe.core.graphql.config

import com.gu.recipe.core.graphql.provider.DefaultFeastGraphQlServerUrlProvider
import com.gu.recipe.core.graphql.provider.GraphQlServerUrlProvider

enum class GraphQlEnvironment(
    val baseUrl: String,
) {
    CODE("https://recipes.code.dev-guardianapis.com"),
    PROD("https://recipes.guardianapis.com"),
}

data class GraphQlConfig(
    val baseUrl: String,
    val serverUrlProvider: GraphQlServerUrlProvider = DefaultFeastGraphQlServerUrlProvider,
) {
    constructor(
        environment: GraphQlEnvironment,
        serverUrlProvider: GraphQlServerUrlProvider = DefaultFeastGraphQlServerUrlProvider,
    ) : this(
        baseUrl = environment.baseUrl,
        serverUrlProvider = serverUrlProvider,
    )

    val serverUrl: String
        get() = serverUrlProvider.serverUrl(baseUrl)
}