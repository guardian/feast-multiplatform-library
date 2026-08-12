package com.gu.recipe.api.repository

import com.gu.recipe.backend.graphql.GraphQlResult
import com.gu.recipe.backend.graphql.generated.GetFrontsByRegionQuery
import com.gu.recipe.backend.graphql.generated.type.Editions
import com.gu.recipe.backend.graphql.generated.type.Regions
import com.gu.recipe.backend.graphql.repository.RecipeGraphQlDataSource

internal class GraphQlRepositoryImpl(
    private val dataSource: RecipeGraphQlDataSource,
) : GraphQLRepository {

    override suspend fun getFrontByRegion(
        region: Regions,
        edition: Editions,
        recipesLimit: Int
    ): GraphQlResult<List<GetFrontsByRegionQuery.Front>> {
        return dataSource.getFrontByRegion(
            region = region,
            edition = edition,
            recipesLimit = recipesLimit,
        )
    }
}