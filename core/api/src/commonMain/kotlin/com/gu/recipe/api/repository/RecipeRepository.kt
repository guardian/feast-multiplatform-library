package com.gu.recipe.api.repository

import com.gu.recipe.core.graphql.generated.CurationForTestQuery
import com.gu.recipe.core.graphql.generated.GetFrontsByRegionQuery
import com.gu.recipe.core.graphql.generated.type.Editions
import com.gu.recipe.core.graphql.generated.type.Regions

interface RecipeRepository {
    suspend fun getFrontByRegion(
        region: Regions,
        edition: Editions,
        recipesLimit: Int
    ): Result<List<GetFrontsByRegionQuery.Front>>

    suspend fun getCurationForTest(
        region: Regions,
        edition: Editions
    ): Result<CurationForTestQuery.Data>
}
