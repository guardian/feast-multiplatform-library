package com.gu.recipe.api.config

import com.gu.recipe.core.graphql.config.GraphQlConfig
import com.gu.recipe.core.graphql.provider.DefaultFeastGraphQlServerUrlProvider
import com.gu.recipe.core.graphql.provider.FixedGraphQlServerUrlProvider
import com.gu.recipe.core.networking.NetworkConfig

data class FeastApiConfig(
    val networkConfig: NetworkConfig,
)

internal fun FeastApiConfig.toGraphQlConfig(): GraphQlConfig =
    GraphQlConfig(
        networkConfig = networkConfig,
    )

