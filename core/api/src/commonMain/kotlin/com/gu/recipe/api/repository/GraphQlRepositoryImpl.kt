package com.gu.recipe.api.repository

import com.gu.recipe.core.graphql.GraphQlResult
import com.gu.recipe.core.graphql.client.FeastGraphQlClient
import com.gu.recipe.core.graphql.generated.GetFrontsByRegionQuery
import com.gu.recipe.core.graphql.generated.type.Editions
import com.gu.recipe.core.graphql.generated.type.Query
import com.gu.recipe.core.graphql.generated.type.Regions
import com.gu.recipe.core.graphql.repository.RecipeGraphQlDataSource

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