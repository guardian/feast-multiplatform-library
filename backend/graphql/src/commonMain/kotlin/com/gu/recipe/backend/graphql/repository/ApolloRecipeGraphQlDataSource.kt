package com.gu.recipe.backend.graphql.repository

import com.gu.recipe.backend.graphql.GraphQlResult
import com.gu.recipe.backend.graphql.client.FeastGraphQlClient
import com.gu.recipe.backend.graphql.generated.GetDishOfTheDayRecipeQuery
import com.gu.recipe.backend.graphql.generated.GetFrontsByRegionQuery
import com.gu.recipe.backend.graphql.generated.type.Editions
import com.gu.recipe.backend.graphql.generated.type.Regions

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

    override suspend fun getDishOfTheDayContainer(
        region: Regions,
        edition: Editions,
    ): GraphQlResult<GetDishOfTheDayRecipeQuery.Container?> {
        val result = feastGraphQlClient.query(
            GetDishOfTheDayRecipeQuery(
                region = region,
                edition = edition,
                alias = DISH_OF_THE_DAY_RECIPE_ALIAS
            )
        )
        return if (result is GraphQlResult.Success) {
            GraphQlResult.Success(result.value.Container)
        } else {
            result as GraphQlResult.Failure
        }
    }
}

private const val DISH_OF_THE_DAY_RECIPE_ALIAS = "dotd"