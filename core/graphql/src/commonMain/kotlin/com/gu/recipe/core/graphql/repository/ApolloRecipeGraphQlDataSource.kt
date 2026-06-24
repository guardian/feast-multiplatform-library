package com.gu.recipe.core.graphql.repository

import com.apollographql.apollo.api.Optional
import com.gu.recipe.core.graphql.GraphQlResult
import com.gu.recipe.core.graphql.client.FeastGraphQlClient
import com.gu.recipe.core.graphql.generated.GetFrontsByRegionQuery
import com.gu.recipe.core.graphql.generated.type.Editions
import com.gu.recipe.core.graphql.generated.type.Regions
import kotlinx.datetime.LocalDate

class ApolloRecipeGraphQlDataSource(
    private val feastGraphQlClient: FeastGraphQlClient,
) : RecipeGraphQlDataSource {

    override suspend fun getFrontByRegion(
        region: Regions, edition: Editions, date: LocalDate, recipesLimit: Int
    ): GraphQlResult<List<GetFrontsByRegionQuery.Front>> {
        return when (val result = feastGraphQlClient.query(
            GetFrontsByRegionQuery(
                region = region, edition = edition, date = Optional.Absent, recipesLimit2 = recipesLimit
            )
        )) {
            is GraphQlResult.Success -> GraphQlResult.Success(result.value.Front)
            is GraphQlResult.Failure -> result
        }
    }
}


