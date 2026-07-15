package com.gu.recipe.core.graphql.repository

import com.gu.recipe.core.graphql.GraphQlResult
import com.gu.recipe.core.graphql.client.FeastGraphQlClient
import com.gu.recipe.core.graphql.generated.GetFrontsByRegionQuery
import com.gu.recipe.core.graphql.generated.CurationForTestQuery
import com.gu.recipe.core.graphql.generated.type.Editions
import com.gu.recipe.core.graphql.generated.type.Regions

class ApolloRecipeGraphQlDataSource(
    private val feastGraphQlClient: FeastGraphQlClient,
) : RecipeGraphQlDataSource {

    override suspend fun getFrontByRegion(
        region: Regions, edition: Editions, recipesLimit: Int
    ): GraphQlResult<List<GetFrontsByRegionQuery.Front>> {
        return when (val result = feastGraphQlClient.query(
            GetFrontsByRegionQuery(
                region = region,
                edition = edition,
                recipesLimit2 = recipesLimit,
            )
        )) {
            is GraphQlResult.Success -> GraphQlResult.Success(result.value.Front)
            is GraphQlResult.Failure -> result
        }
    }

    override suspend fun getCurationForTest(
        region: Regions,
        edition: Editions
    ): GraphQlResult<List<CurationForTest.Front>> {
        return when (val result = feastGraphQlClient.query(
            CurationForTestQuery(
                region = region,
                edition = edition
            )
        )) {
            is GraphQlResult.Success -> GraphQlResult.Success(result.value)
            is GraphQlResult.Failure -> result
        }
    }
}


