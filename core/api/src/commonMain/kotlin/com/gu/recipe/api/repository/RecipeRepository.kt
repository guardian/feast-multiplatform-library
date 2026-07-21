package com.gu.recipe.api.repository

import com.gu.recipe.core.graphql.generated.CurationForTestQuery
import com.gu.recipe.core.graphql.generated.GetFrontsByRegionQuery
import com.gu.recipe.core.graphql.generated.type.Editions
import com.gu.recipe.core.graphql.generated.type.Regions
import kotlin.coroutines.cancellation.CancellationException

interface RecipeRepository {
    suspend fun getFrontByRegion(
        region: Regions,
        edition: Editions,
        recipesLimit: Int
    ): Result<List<GetFrontsByRegionQuery.Front>>

    @Throws(RecipeRepositoryError::class, CancellationException::class)
    suspend fun getCurationForTest(
        region: Regions,
        edition: Editions
    ): CurationForTestQuery.Data
}
