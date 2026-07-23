package com.gu.recipe.core.graphql.repository

import com.gu.recipe.core.graphql.GraphQlResult
import com.gu.recipe.core.graphql.client.FeastGraphQlClient
import com.gu.recipe.core.graphql.generated.GetFrontsByRegionQuery
import com.gu.recipe.core.graphql.generated.type.Editions
import com.gu.recipe.core.graphql.generated.type.Regions

class ApolloRecipeGraphQlDataSource(
    private val feastGraphQlClient: FeastGraphQlClient,
) : RecipeGraphQlDataSource {

    override suspend fun getFrontByRegion(
        region: Regions, edition: Editions, recipesLimit: Int
    ): GraphQlResult<List<GetFrontsByRegionQuery.Front>> {
        val result = feastGraphQlClient.query(
            GetFrontsByRegionQuery(
                region = region,
                edition = edition,
                recipesLimit2 = recipesLimit,
            ),
        )
        return if (result is GraphQlResult.Success) {
            GraphQlResult.Success(result.value.Front)
        } else {
            result as GraphQlResult.Failure
        }
    }
}