package com.gu.recipe.core.networking

sealed interface NetworkError {
    data object Unauthorized : NetworkError

    data class Http(
        val statusCode: Int,
        val message: String? = null,
        val body: String? = null,
    ) : NetworkError

    data class Serialization(
        val message: String? = null,
        val causeMessage: String? = null,
    ) : NetworkError

    data class Connectivity(
        val message: String? = null,
        val causeMessage: String? = null,
    ) : NetworkError

    data class Unknown(
        val message: String? = null,
        val causeMessage: String? = null,
    ) : NetworkError
}

