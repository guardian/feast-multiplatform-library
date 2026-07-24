package com.gu.recipe.core.graphql

sealed interface GraphQlError {
    data class GraphQl(
        val messages: List<String>,
    ) : GraphQlError

    data class Transport(
        val cause: Throwable,
    ) : GraphQlError

    data class Unexpected(
        val cause: Throwable,
    ) : GraphQlError

    data object MissingData : GraphQlError
}

