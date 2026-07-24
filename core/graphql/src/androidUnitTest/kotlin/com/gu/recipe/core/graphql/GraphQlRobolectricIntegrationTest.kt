package com.gu.recipe.core.graphql

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Query
import com.apollographql.cache.normalized.FetchPolicy
import com.apollographql.cache.normalized.fetchPolicy
import com.apollographql.cache.normalized.sql.SqlNormalizedCacheFactory
import com.gu.recipe.core.graphql.client.ApolloClientFactory
import com.gu.recipe.core.graphql.client.FeastGraphQlClient
import com.gu.recipe.core.graphql.config.GraphQlConfig
import com.gu.recipe.core.graphql.generated.GetFrontsByRegionQuery
import com.gu.recipe.core.graphql.generated.type.Editions
import com.gu.recipe.core.graphql.generated.type.Regions
import com.gu.recipe.core.graphql.repository.ApolloRecipeGraphQlDataSource
import com.gu.recipe.core.networking.FeastNetworkApiEndpoint
import com.gu.recipe.core.networking.NetworkConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.fail

@Ignore("Hits a live GraphQL endpoint; run locally when needed.")
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class GraphQlRobolectricIntegrationTest {

    @Test
    fun `given a live query when fetched with network only then it is persisted and can be read with cache only`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val cacheName = "feast_graphql_test_${System.nanoTime()}.db"
        context.deleteDatabase(cacheName)

        val apolloClient = createApolloClient(context, cacheName)
        val query = query(region = Regions.northern, edition = Editions.all)

        try {
            val networkResponse = apolloClient
                .query(query)
                .fetchPolicy(FetchPolicy.NetworkOnly)
                .execute()

            assertNull(networkResponse.exception, networkResponse.exception?.message)
            val networkFronts = networkResponse.data?.Front.orEmpty()
            assertTrue(networkFronts.isNotEmpty(), "Expected at least one front from the live GraphQL endpoint")

            val cacheOnlyResponse = apolloClient
                .query(query)
                .fetchPolicy(FetchPolicy.CacheOnly)
                .execute()

            assertNull(cacheOnlyResponse.exception, cacheOnlyResponse.exception?.message)
            val cacheFronts = cacheOnlyResponse.data?.Front.orEmpty()
            assertTrue(cacheFronts.isNotEmpty(), "Expected the cached query to return previously persisted fronts")
            assertEquals(
                networkFronts.map { it.title },
                cacheFronts.map { it.title },
                "Expected cache-only query to replay the same front titles fetched from the network",
            )
        } finally {
            context.deleteDatabase(cacheName)
        }
    }

    @Test
    fun `given live data source when querying fronts by region then it returns fronts through real graphql client`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val cacheName = "feast_graphql_datasource_${System.nanoTime()}.db"
        context.deleteDatabase(cacheName)

        val dataSource = ApolloRecipeGraphQlDataSource(
            FeastGraphQlClient(createApolloClient(context, cacheName)),
        )

        try {
            when (val result = dataSource.getFrontByRegion(
                region = Regions.northern,
                edition = Editions.all,
                recipesLimit = 2,
            )) {
                is GraphQlResult.Success -> {
                    assertTrue(result.value.isNotEmpty(), "Expected live GraphQL datasource to return fronts")
                    assertNotNull(result.value.first().title)
                }

                is GraphQlResult.Failure -> fail("Expected success from live GraphQL datasource but got $result")
            }
        } finally {
            context.deleteDatabase(cacheName)
        }
    }

    private fun createApolloClient(context: Context, cacheName: String): ApolloClient {
        val config = GraphQlConfig(
            networkConfig = NetworkConfig(endpoint = FeastNetworkApiEndpoint.CODE),
        )

        return ApolloClientFactory(Dispatchers.IO).create(
            config = config,
            /*normalizedCacheFactory = SqlNormalizedCacheFactory(
                context = context.applicationContext,
                name = cacheName,
            ),*/
        )
    }

    private fun query(region: Regions, edition: Editions): Query<GetFrontsByRegionQuery.Data> =
        GetFrontsByRegionQuery(
            region = region,
            edition = edition,
            recipesLimit2 = 2,
        )
}


