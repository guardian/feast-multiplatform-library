package com.gu.recipe.api.repository

import com.gu.recipe.api.models.FrontResponse
import com.gu.recipe.core.graphql.generated.GetFrontsByRegionQuery
import com.gu.recipe.core.graphql.generated.type.Editions
import com.gu.recipe.core.graphql.generated.type.Regions
import kotlinx.datetime.LocalDate

interface RecipeRepository {
    suspend fun getFrontByRegion(region: Regions,
                                 edition: Editions,
                                 date: LocalDate,
                                 recipesLimit: Int
    ): Result<List<GetFrontsByRegionQuery.Front>>
}
