package com.gu.recipe.api.repository

import com.gu.recipe.core.graphql.GraphQlResult
import com.gu.recipe.core.graphql.generated.GetFrontsByRegionQuery
import com.gu.recipe.core.graphql.generated.type.Editions
import com.gu.recipe.core.graphql.generated.type.Regions

interface GraphQLRepository {
    suspend fun getFrontByRegion(
        region: Regions,
        edition: Editions,
        recipesLimit: Int
    ): GraphQlResult<List<GetFrontsByRegionQuery.Front>>
}
