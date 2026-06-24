package com.gu.recipe.core.networking

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

data class NetworkConfig(
    val endpoint: FeastNetworkApiEndpoint,
    val baseUrlProvider: BaseUrlProvider = FeastEndpointBaseUrlProvider(endpoint),
    val accessTokenProvider: AccessTokenProvider = NoOpAccessTokenProvider,
    val logger: NetworkLogger = NoOpNetworkLogger,
    val connectTimeout: Duration = DEFAULT_CONNECT_TIMEOUT,
    val readTimeout: Duration = DEFAULT_READ_TIMEOUT,
    val writeTimeout: Duration = DEFAULT_WRITE_TIMEOUT,
) {
    companion object {
        val DEFAULT_CONNECT_TIMEOUT: Duration = 15.seconds
        val DEFAULT_READ_TIMEOUT: Duration = 30.seconds
        val DEFAULT_WRITE_TIMEOUT: Duration = 30.seconds
    }
}


