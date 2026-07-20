package com.gu.recipe.api.repository

import com.gu.recipe.api.models.FrontResponse
import com.gu.recipe.core.graphql.GraphQlError
import com.gu.recipe.core.graphql.GraphQlResult
import com.gu.recipe.core.graphql.generated.CurationForTestQuery
import com.gu.recipe.core.graphql.generated.GetFrontsByRegionQuery
import com.gu.recipe.core.graphql.generated.type.Editions
import com.gu.recipe.core.graphql.generated.type.Regions
import com.gu.recipe.core.graphql.model.GraphQlRecipe
import com.gu.recipe.core.graphql.repository.RecipeGraphQlDataSource
import kotlin.coroutines.cancellation.CancellationException

class GraphQlRecipeRepository(
    private val dataSource: RecipeGraphQlDataSource,
) : RecipeRepository {

    override suspend fun getFrontByRegion(
        region: Regions,
        edition: Editions,
        recipesLimit: Int
    ): Result<List<GetFrontsByRegionQuery.Front>> {
        return when (val result = dataSource.getFrontByRegion(
            region = region,
            edition = edition,
            recipesLimit = recipesLimit,
        )) {
            is GraphQlResult.Success -> {
                val front = result.value
                Result.success(front)
            }

            is GraphQlResult.Failure -> Result.failure(result.error.toRepositoryError())
        }
    }

    @Throws(RecipeRepositoryError::class, CancellationException::class)
    override suspend fun getCurationForTest(
        region: Regions,
        edition: Editions
    ): CurationForTestQuery.Data {
        return when (val result = dataSource.getCurationForTest(
            region = region,
            edition = edition
        )) {
            is GraphQlResult.Success -> result.value
            is GraphQlResult.Failure -> throw result.error.toRepositoryError()
        }
    }

    private fun GraphQlError.toRepositoryError(): Exception = when (this) {
        is GraphQlError.GraphQl -> RecipeRepositoryError.GraphQl(messages)
        is GraphQlError.Transport -> RecipeRepositoryError.Network(cause)
        is GraphQlError.Unexpected -> RecipeRepositoryError.Unexpected(cause)
        GraphQlError.MissingData -> RecipeRepositoryError.MissingData
    }

    private fun GraphQlRecipe.toAPIResult(): FrontResponse =
        FrontResponse(
            id = id,
        )
}

sealed class RecipeRepositoryError : Exception() {
    data class GraphQl(val messages: List<String>) : Exception()
    data class Network(override val cause: Throwable) : Exception()
    data class Unexpected(override val cause: Throwable) : Exception()
    object MissingData : Exception()
}

