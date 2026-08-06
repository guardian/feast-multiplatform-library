package com.gu.recipe.core.graphql.client

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.Mutation
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.Query
import com.apollographql.apollo.exception.ApolloException
import com.gu.recipe.core.graphql.GraphQLError
import com.gu.recipe.core.graphql.GraphQlResult

class FeastGraphQlClient(
    private val apolloClient: ApolloClient,
) {
    suspend fun <D : Query.Data> query(query: Query<D>): GraphQlResult<D> =
        try {
            mapResponse(apolloClient.query(query).execute())
        } catch (exception: ApolloException) {
            GraphQlResult.Failure(GraphQLError.Transport(exception))
        } catch (exception: Throwable) {
            GraphQlResult.Failure(GraphQLError.Unexpected(exception))
        }

    suspend fun <D : Mutation.Data> mutate(mutation: Mutation<D>): GraphQlResult<D> =
        try {
            mapResponse(apolloClient.mutation(mutation).execute())
        } catch (exception: ApolloException) {
            GraphQlResult.Failure(GraphQLError.Transport(exception))
        } catch (exception: Throwable) {
            GraphQlResult.Failure(GraphQLError.Unexpected(exception))
        }

    private fun <D : Operation.Data> mapResponse(response: ApolloResponse<D>): GraphQlResult<D> {
        val transportException = response.exception
        if (transportException != null) {
            return GraphQlResult.Failure(GraphQLError.Transport(transportException))
        }

        val graphQlErrors = response.errors
        if (!graphQlErrors.isNullOrEmpty()) {
            return GraphQlResult.Failure(
                GraphQLError.GraphQL(graphQlErrors.map { graphQlError -> graphQlError.message }),
            )
        }

        val data = response.data ?: return GraphQlResult.Failure(GraphQLError.MissingData)
        return GraphQlResult.Success(data)
    }
}