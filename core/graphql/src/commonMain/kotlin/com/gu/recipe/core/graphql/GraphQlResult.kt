package com.gu.recipe.core.graphql

sealed interface GraphQlResult<out T> {
    data class Success<T>(
        val value: T,
    ) : GraphQlResult<T>

    data class Failure(
        val error: GraphQlError,
    ) : GraphQlResult<Nothing>
}

inline fun <T, R> GraphQlResult<T>.map(transform: (T) -> R): GraphQlResult<R> = when (this) {
    is GraphQlResult.Success -> GraphQlResult.Success(transform(value))
    is GraphQlResult.Failure -> this
}

inline fun <T> GraphQlResult<T>.getOrElse(defaultValue: (GraphQlError) -> T): T = when (this) {
    is GraphQlResult.Success -> value
    is GraphQlResult.Failure -> defaultValue(error)
}

