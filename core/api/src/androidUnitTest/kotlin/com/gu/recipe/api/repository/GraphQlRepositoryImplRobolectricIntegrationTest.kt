package com.gu.recipe.api.repository

import com.gu.recipe.api.config.FeastApiConfig
import com.gu.recipe.api.di.feastApiModule
import com.gu.recipe.core.graphql.GraphQlResult
import com.gu.recipe.core.graphql.generated.type.Editions
import com.gu.recipe.core.graphql.generated.type.Regions
import com.gu.recipe.core.networking.FeastNetworkApiEndpoint
import com.gu.recipe.core.networking.NetworkConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.koin.dsl.koinApplication
import kotlin.test.assertTrue

@Ignore("This test is ignored for CI, please use this locally to test the live GraphQL endpoint")
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class GraphQlRepositoryImplRobolectricIntegrationTest {

    @Test
    fun `live repository call returns fronts on the JVM`() = runTest {
        val application = koinApplication {
            modules(
                feastApiModule(
                    config = FeastApiConfig(
                        networkConfig = NetworkConfig(endpoint = FeastNetworkApiEndpoint.CODE),
                    ),
                    ioDispatcher = Dispatchers.IO,
                ),
            )
        }

        try {
            val repository = application.koin.get<GraphQLRepository>()
            val result = repository.getFrontByRegion(
                region = Regions.northern,
                edition = Editions.all,
                recipesLimit = 2,
            )
            assertTrue(
                actual = result is GraphQlResult.Success
            )
        } finally {
            application.close()
        }
    }
}


