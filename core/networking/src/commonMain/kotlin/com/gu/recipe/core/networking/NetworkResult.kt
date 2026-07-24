package com.gu.recipe.core.networking

sealed interface NetworkResult<out T> {
    data class Success<T>(val value: T) : NetworkResult<T>

    data class Failure(val error: NetworkError) : NetworkResult<Nothing>
}

inline fun <T, R> NetworkResult<T>.map(transform: (T) -> R): NetworkResult<R> =
    when (this) {
        is NetworkResult.Success -> NetworkResult.Success(transform(value))
        is NetworkResult.Failure -> this
    }

inline fun <T> NetworkResult<T>.getOrElse(defaultValue: (NetworkError) -> T): T =
    when (this) {
        is NetworkResult.Success -> value
        is NetworkResult.Failure -> defaultValue(error)
    }

