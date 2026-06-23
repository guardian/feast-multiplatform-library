package com.gu.recipe.core.graphql.repository

import com.gu.recipe.core.graphql.GraphQlResult
import com.gu.recipe.core.graphql.generated.GetFrontsByRegionQuery
import com.gu.recipe.core.graphql.generated.type.Editions
import com.gu.recipe.core.graphql.generated.type.Regions
import kotlinx.datetime.LocalDate

interface RecipeGraphQlDataSource {
    suspend fun getFrontByRegion(
        region: Regions,
        edition: Editions,
        date: LocalDate,
        recipesLimit: Int,
    ): GraphQlResult<List<GetFrontsByRegionQuery.Front>>
}