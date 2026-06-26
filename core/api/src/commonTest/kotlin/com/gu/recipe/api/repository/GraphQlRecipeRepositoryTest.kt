package com.gu.recipe.api.repository

import com.gu.recipe.core.graphql.GraphQlResult
import com.gu.recipe.core.graphql.generated.GetFrontsByRegionQuery
import com.gu.recipe.core.graphql.generated.type.Editions
import com.gu.recipe.core.graphql.generated.type.Regions
import com.gu.recipe.core.graphql.repository.RecipeGraphQlDataSource

private class FakeRecipeGraphQlDataSource(
    private val result: GraphQlResult<List<GetFrontsByRegionQuery.Front>>,
) : RecipeGraphQlDataSource {

    override suspend fun getFrontByRegion(
        region: Regions,
        edition: Editions,
        recipesLimit: Int
    ): GraphQlResult<List<GetFrontsByRegionQuery.Front>> = result
}

private fun suspendTest(block: suspend () -> Unit) {
    kotlinx.coroutines.test.runTest {
        block()
    }
}

