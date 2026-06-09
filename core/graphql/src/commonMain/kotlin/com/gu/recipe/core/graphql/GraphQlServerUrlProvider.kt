package com.gu.recipe.core.graphql

import com.gu.recipe.core.networking.NetworkConfig

fun interface GraphQlServerUrlProvider {
    fun serverUrl(networkConfig: NetworkConfig): String
}

data class FixedGraphQlServerUrlProvider(
    private val value: String,
) : GraphQlServerUrlProvider {
    override fun serverUrl(networkConfig: NetworkConfig): String = value.normalizedGraphQlUrl()
}

object DefaultFeastGraphQlServerUrlProvider : GraphQlServerUrlProvider {
    override fun serverUrl(networkConfig: NetworkConfig): String =
        "${networkConfig.baseUrlProvider.baseUrl().trimEnd('/')}/graphql"
}

private fun String.normalizedGraphQlUrl(): String = trim().removeSuffix("/")

