package com.gu.recipe.backend.graphql

import com.apollographql.apollo.ApolloClient
import com.gu.recipe.backend.graphql.client.FeastGraphQlClient
import com.gu.recipe.backend.graphql.config.GraphQlConfig
import com.gu.recipe.backend.graphql.di.GraphQlQualifiers
import com.gu.recipe.backend.graphql.di.graphQlModule
import com.gu.recipe.backend.graphql.provider.FixedGraphQlServerUrlProvider
import com.gu.recipe.backend.graphql.provider.GraphQlServerUrlProvider
import com.gu.recipe.backend.graphql.repository.RecipeGraphQlDataSource
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.core.context.stopKoin
import org.koin.core.qualifier.named
import org.koin.dsl.koinApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class GraphQlConfigTest {

    @Test
    fun `default GraphQL url is derived from the configured base url`() {
        val config = GraphQlConfig(
            baseUrl = "https://recipes.code.dev-guardianapis.com",
        )

        assertEquals(
            expected = "https://recipes.code.dev-guardianapis.com/graphql",
            actual = config.serverUrl,
        )
    }

    @Test
    fun `fixed GraphQL url provider trims the trailing slash`() {
        val config = GraphQlConfig(
            baseUrl = "https://recipes.guardianapis.com",
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
            baseUrl = "https://recipes.guardianapis.com",
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
            assertNotNull(koin.get<RecipeGraphQlDataSource>())
        } finally {
            stopKoin()
        }
    }
}

