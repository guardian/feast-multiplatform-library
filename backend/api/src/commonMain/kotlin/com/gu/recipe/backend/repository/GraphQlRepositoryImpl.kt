package com.gu.recipe.backend.repository

import com.gu.recipe.backend.graphql.GraphQlResult
import com.gu.recipe.backend.graphql.GraphQLError
import com.gu.recipe.backend.graphql.generated.CuratedContainerByIdQuery
import com.gu.recipe.backend.graphql.generated.GetDishOfTheDayRecipeQuery
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

    override suspend fun getDishOfTheDayContainer(
        region: Regions,
        edition: Editions,
    ): GraphQlResult<GetDishOfTheDayRecipeQuery.Container?> {
        return dataSource.getDishOfTheDayContainer(
            region = region,
            edition = edition,
        )
    }

    override suspend fun getCuratedCollection(
        collectionId: String
    ): GraphQlResult<CuratedContainerByIdQuery.CuratedContainerById?> {
        if (!UUID_REGEX.matches(collectionId)) {
            return GraphQlResult.Failure(
                GraphQLError.Unexpected(
                    IllegalArgumentException("Invalid collectionId UUID: $collectionId")
                )
            )
        }

        return dataSource.getCuratedCollection(
            collectionId = collectionId,
        )
    }

}

private val UUID_REGEX =
    Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$")
