package com.gu.recipe.core.graphql

import com.apollographql.apollo.ApolloClient
import com.gu.recipe.core.networking.FeastNetworkApiEndpoint
import com.gu.recipe.core.networking.NetworkConfig
import kotlinx.coroutines.CoroutineDispatcher
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import org.koin.core.context.stopKoin
import org.koin.core.qualifier.named
import org.koin.dsl.koinApplication

class GraphQlConfigTest {
    @Test
    fun `default GraphQL url is derived from the networking base url`() {
        val config = GraphQlConfig(
            networkConfig = NetworkConfig(endpoint = FeastNetworkApiEndpoint.CODE),
        )

        assertEquals(
            expected = "https://recipes.code.dev-guardianapis.com/graphql",
            actual = config.serverUrl,
        )
    }

    @Test
    fun `fixed GraphQL url provider trims the trailing slash`() {
        val config = GraphQlConfig(
            networkConfig = NetworkConfig(endpoint = FeastNetworkApiEndpoint.PROD),
            serverUrlProvider = FixedGraphQlServerUrlProvider("https://recipes.guardianapis.com/graphql/"),
        )

        assertEquals(
            expected = "https://recipes.guardianapis.com/graphql",
            actual = config.serverUrl,
        )
    }

    @Test
    fun `koin module exposes graphql dependencies`() {
        val config = GraphQlConfig(
            networkConfig = NetworkConfig(endpoint = FeastNetworkApiEndpoint.PROD),
        )
        val application = koinApplication {
            modules(graphQlModule(config))
        }

        try {
            val koin = application.koin
            assertNotNull(koin.get<GraphQlConfig>())
            assertNotNull(koin.get<GraphQlServerUrlProvider>())
            assertNotNull(koin.get<CoroutineDispatcher>(named(GraphQlQualifiers.IoDispatcher)))
            assertNotNull(koin.get<ApolloClient>())
            assertNotNull(koin.get<FeastGraphQlClient>())
        } finally {
            stopKoin()
        }
    }
}

