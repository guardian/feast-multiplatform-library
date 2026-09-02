package com.gu.recipe.backend.graphql.repository

import com.gu.recipe.backend.graphql.GraphQlResult
import com.gu.recipe.backend.graphql.generated.CuratedContainerByIdQuery
import com.gu.recipe.backend.graphql.generated.GetDishOfTheDayRecipeQuery
import com.gu.recipe.backend.graphql.generated.GetFrontsByRegionQuery
import com.gu.recipe.backend.graphql.generated.type.Editions
import com.gu.recipe.backend.graphql.generated.type.Regions

interface RecipeGraphQlDataSource {
    suspend fun getFrontByRegion(
        region: Regions,
        edition: Editions,
        recipesLimit: Int,
    ): GraphQlResult<List<GetFrontsByRegionQuery.Front>>

    suspend fun getDishOfTheDayContainer(
        region: Regions,
        edition: Editions,
    ): GraphQlResult<GetDishOfTheDayRecipeQuery.Container?>

    suspend fun getCuratedCollection(
        collectionId: String
    ): GraphQlResult<CuratedContainerByIdQuery.CuratedContainerById?>
}