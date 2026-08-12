package com.gu.recipe.api.repository

import com.gu.recipe.api.di.feastApiModule
import com.gu.recipe.backend.graphql.GraphQlResult
import com.gu.recipe.backend.graphql.generated.type.Editions
import com.gu.recipe.backend.graphql.generated.type.Regions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.dsl.koinApplication
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertTrue

@Ignore("Hits a live GraphQL endpoint; run locally when needed.")
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class GraphQlRepositoryImplRobolectricIntegrationTest {

    @Test
    fun `live repository call returns fronts on the JVM`() = runTest {
        val application = koinApplication {
            modules(
                feastApiModule(
                    baseUrl = "https://recipes.code.dev-guardianapis.com",
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


