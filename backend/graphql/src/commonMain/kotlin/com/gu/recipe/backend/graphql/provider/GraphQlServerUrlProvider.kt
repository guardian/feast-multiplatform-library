package com.gu.recipe.backend.graphql.provider

fun interface GraphQlServerUrlProvider {
    fun serverUrl(baseUrl: String): String
}

data class FixedGraphQlServerUrlProvider(
    private val value: String,
) : GraphQlServerUrlProvider {
    override fun serverUrl(baseUrl: String): String = value.normalizedGraphQlUrl()
}

object DefaultFeastGraphQlServerUrlProvider : GraphQlServerUrlProvider {
    override fun serverUrl(baseUrl: String): String =
        "${baseUrl.normalizedBaseUrl()}/graphql"
}

private fun String.normalizedGraphQlUrl(): String = trim().removeSuffix("/")

private fun String.normalizedBaseUrl(): String = trim().removeSuffix("/")
