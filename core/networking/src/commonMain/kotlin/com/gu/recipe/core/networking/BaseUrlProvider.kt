package com.gu.recipe.core.networking

/**
 * Consumer-agnostic endpoint type used by the internal networking layer.
 */
enum class FeastEndpointType {
    CODE,
    PROD,
}

/**
 * Enumeration of the available Feast API endpoints.
 */
enum class FeastNetworkApiEndpoint(val value: String) {
    CODE("https://recipes.code.dev-guardianapis.com"),
    PROD("https://recipes.code.dev-guardianapis.com"),
}

fun FeastEndpointType.toNetworkType(): FeastNetworkApiEndpoint = when (this) {
    FeastEndpointType.CODE -> FeastNetworkApiEndpoint.CODE
    FeastEndpointType.PROD -> FeastNetworkApiEndpoint.PROD
}

fun FeastNetworkApiEndpoint.toDomain(): FeastEndpointType = when (this) {
    FeastNetworkApiEndpoint.CODE -> FeastEndpointType.CODE
    FeastNetworkApiEndpoint.PROD -> FeastEndpointType.PROD
}

fun interface BaseUrlProvider {
    fun baseUrl(): String
}

data class FixedBaseUrlProvider(
    private val value: String,
) : BaseUrlProvider {
    override fun baseUrl(): String = value.normalizedBaseUrl()
}

data class FeastEndpointBaseUrlProvider(
    private val endpoint: FeastNetworkApiEndpoint,
) : BaseUrlProvider {
    override fun baseUrl(): String = endpoint.value.normalizedBaseUrl()
}

private fun String.normalizedBaseUrl(): String =
    if (endsWith('/')) this else "$this/"

