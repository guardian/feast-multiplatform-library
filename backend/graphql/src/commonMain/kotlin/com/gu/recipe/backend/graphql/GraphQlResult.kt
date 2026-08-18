package com.gu.recipe.backend.graphql

sealed interface GraphQlResult<out T> {
    data class Success<T>(
        val value: T,
    ) : GraphQlResult<T>

    data class Failure(
        val error: GraphQLError,
    ) : GraphQlResult<Nothing>
}

inline fun <T, R> GraphQlResult<T>.map(transform: (T) -> R): GraphQlResult<R> = when (this) {
    is GraphQlResult.Success -> GraphQlResult.Success(transform(value))
    is GraphQlResult.Failure -> this
}

inline fun <T> GraphQlResult<T>.getOrElse(defaultValue: (GraphQLError) -> T): T = when (this) {
    is GraphQlResult.Success -> value
    is GraphQlResult.Failure -> defaultValue(error)
}

