package com.gu.recipe.core.networking

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class NetworkContractsTest {
    @Test
    fun `fixed base url provider normalizes configured url`() {
        val provider = FixedBaseUrlProvider("https://example.com/")

        assertEquals("https://example.com/", provider.baseUrl())
    }

    @Test
    fun `fixed base url provider adds trailing slash when missing`() {
        val provider = FixedBaseUrlProvider("https://example.com")

        assertEquals("https://example.com/", provider.baseUrl())
    }

    @Test
    fun `endpoint type maps to networking endpoint`() {
        assertEquals(FeastNetworkApiEndpoint.CODE, FeastEndpointType.CODE.toNetworkType())
        assertEquals(FeastNetworkApiEndpoint.PROD, FeastEndpointType.PROD.toNetworkType())
    }

    @Test
    fun `networking endpoint maps back to domain endpoint`() {
        assertEquals(FeastEndpointType.CODE, FeastNetworkApiEndpoint.CODE.toDomain())
        assertEquals(FeastEndpointType.PROD, FeastNetworkApiEndpoint.PROD.toDomain())
    }

    @Test
    fun `endpoint base url provider resolves feast endpoint url`() {
        val provider = FeastEndpointBaseUrlProvider(FeastNetworkApiEndpoint.CODE)

        assertEquals("https://recipes.code.dev-guardianapis.com/", provider.baseUrl())
    }

    @Test
    fun `empty request headers provider returns empty map`() {
        assertEquals(null, NoOpAccessTokenProvider.token)
    }

    @Test
    fun `network result map transforms success value`() {
        val result: NetworkResult<Int> = NetworkResult.Success(5)

        val mapped = result.map { it * 2 }

        assertEquals(NetworkResult.Success(10), mapped)
    }

    @Test
    fun `network result map preserves failure`() {
        val result: NetworkResult<Int> = NetworkResult.Failure(NetworkError.Unauthorized)

        val mapped = result.map { it * 2 }

        assertIs<NetworkResult.Failure>(mapped)
        assertEquals(NetworkError.Unauthorized, mapped.error)
    }

    @Test
    fun `network config exposes defaults and collaborators`() {
        val accessTokenProvider = object : AccessTokenProvider {
            override val token: String? = "token"

            override suspend fun refresh(): String? = token
        }

        val config = NetworkConfig(
            endpoint = FeastEndpointType.CODE.toNetworkType(),
            accessTokenProvider = accessTokenProvider,
        )

        assertEquals(FeastNetworkApiEndpoint.CODE, config.endpoint)
        assertEquals("https://recipes.code.dev-guardianapis.com/", config.baseUrlProvider.baseUrl())
        assertEquals("token", config.accessTokenProvider.token)
        assertEquals(NetworkConfig.DEFAULT_CONNECT_TIMEOUT, config.connectTimeout)
        assertEquals(NetworkConfig.DEFAULT_READ_TIMEOUT, config.readTimeout)
        assertEquals(NetworkConfig.DEFAULT_WRITE_TIMEOUT, config.writeTimeout)
    }
}


