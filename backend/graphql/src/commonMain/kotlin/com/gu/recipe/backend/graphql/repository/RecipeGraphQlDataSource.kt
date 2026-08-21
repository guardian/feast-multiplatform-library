package com.gu.recipe.backend.graphql.repository

import com.gu.recipe.backend.graphql.GraphQlResult
import com.gu.recipe.backend.graphql.generated.GetFrontsByRegionQuery
import com.gu.recipe.backend.graphql.generated.type.Editions
import com.gu.recipe.backend.graphql.generated.type.Regions

interface RecipeGraphQlDataSource {
    suspend fun getFrontByRegion(
        region: Regions,
        edition: Editions,
        recipesLimit: Int,
    ): GraphQlResult<List<GetFrontsByRegionQuery.Front>>

    suspend fun getDishOfTheDayRecipe(
        region: Regions,
        edition: Editions,
    ): GraphQlResult<Unit>
}