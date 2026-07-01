package com.gu.recipe.api.repository

import com.gu.recipe.api.config.FeastApiConfig
import com.gu.recipe.api.di.feastApiModule
import com.gu.recipe.core.graphql.generated.type.Editions
import com.gu.recipe.core.graphql.generated.type.Regions
import com.gu.recipe.core.networking.FeastNetworkApiEndpoint
import com.gu.recipe.core.networking.NetworkConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.koin.dsl.koinApplication
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class GraphQlRecipeRepositoryRobolectricIntegrationTest {

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
            val repository = application.koin.get<RecipeRepository>()
            val result = repository.getFrontByRegion(
                region = Regions.northern,
                edition = Editions.all,
                recipesLimit = 2,
            )

            assertTrue(result.isSuccess, "Expected live RecipeRepository call to succeed but got ${result.exceptionOrNull()}")
            assertTrue(result.getOrThrow().isNotEmpty(), "Expected live RecipeRepository call to return fronts")
        } finally {
            application.close()
        }
    }
}


