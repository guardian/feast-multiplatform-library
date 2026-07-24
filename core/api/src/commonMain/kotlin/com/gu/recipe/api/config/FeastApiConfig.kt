package com.gu.recipe.api.config

import com.gu.recipe.api.model.FeastEnvironment
import com.gu.recipe.core.graphql.config.GraphQlConfig
import com.gu.recipe.core.networking.FeastNetworkApiEndpoint
import com.gu.recipe.core.networking.NetworkConfig

data class FeastApiConfig(
    val environment: FeastEnvironment,
)

internal fun FeastApiConfig.toGraphQlConfig(): GraphQlConfig {
    val endpoint = when (environment) {
        FeastEnvironment.CODE -> FeastNetworkApiEndpoint.CODE
        FeastEnvironment.PROD -> FeastNetworkApiEndpoint.PROD
    }

    val networkConfig = NetworkConfig(endpoint = endpoint)

    return GraphQlConfig(
        networkConfig = networkConfig
    )
}