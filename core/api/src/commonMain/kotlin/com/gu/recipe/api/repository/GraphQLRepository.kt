package com.gu.recipe.api.repository

import com.gu.recipe.core.graphql.GraphQlResult
import com.gu.recipe.core.graphql.generated.GetFrontsByRegionQuery
import com.gu.recipe.core.graphql.generated.type.Editions
import com.gu.recipe.core.graphql.generated.type.Regions

/**
 * Repository for GraphQL API retrieval operations.
 */
interface GraphQLRepository {
    /**
     * Fetches fronts for a specific region and edition.
     *
     * @param region the target region.
     * @param edition the target edition.
     * @param recipesLimit the maximum number of recipes to return.
     * @return a `GraphQlResult` containing the list of fronts.
     */
    suspend fun getFrontByRegion(
        region: Regions,
        edition: Editions,
        recipesLimit: Int
    ): GraphQlResult<List<GetFrontsByRegionQuery.Front>>
}