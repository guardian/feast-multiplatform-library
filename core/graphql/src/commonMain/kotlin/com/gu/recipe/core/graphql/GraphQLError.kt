package com.gu.recipe.core.graphql

sealed interface GraphQLError {
    data class GraphQL(
        val messages: List<String>,
    ) : GraphQLError

    data class Transport(
        val cause: Throwable,
    ) : GraphQLError

    data class Unexpected(
        val cause: Throwable,
    ) : GraphQLError

    data object MissingData : GraphQLError
}

