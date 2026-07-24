package com.gu.recipe.api.config

import com.gu.recipe.api.model.FeastEnvironment
import com.gu.recipe.core.graphql.config.GraphQlEnvironment
import com.gu.recipe.core.graphql.config.GraphQlConfig

data class FeastApiConfig(
    val environment: FeastEnvironment,
)

internal fun FeastApiConfig.toGraphQlConfig(): GraphQlConfig {
    val graphQlEnvironment = when (environment) {
        FeastEnvironment.CODE -> GraphQlEnvironment.CODE
        FeastEnvironment.PROD -> GraphQlEnvironment.PROD
    }

    return GraphQlConfig(
        environment = graphQlEnvironment,
    )
}