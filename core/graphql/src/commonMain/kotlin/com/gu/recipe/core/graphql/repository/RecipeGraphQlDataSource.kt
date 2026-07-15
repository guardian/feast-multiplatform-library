package com.gu.recipe.core.graphql.repository

import com.gu.recipe.core.graphql.GraphQlResult
import com.gu.recipe.core.graphql.generated.GetFrontsByRegionQuery
import com.gu.recipe.core.graphql.generated.CurationForTestQuery
import com.gu.recipe.core.graphql.generated.type.Editions
import com.gu.recipe.core.graphql.generated.type.Regions

interface RecipeGraphQlDataSource {
    suspend fun getFrontByRegion(
        region: Regions,
        edition: Editions,
        recipesLimit: Int,
    ): GraphQlResult<List<GetFrontsByRegionQuery.Front>>

    suspend fun getCurationForTest(
        region: Regions,
        edition: Editions
    ): GraphQlResult<List<CurationForTest.Front>>
}