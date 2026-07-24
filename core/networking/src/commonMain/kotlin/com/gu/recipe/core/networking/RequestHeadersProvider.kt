package com.gu.recipe.core.networking

interface AccessTokenProvider {
    val token: String?
    suspend fun refresh(): String?
}

object NoOpAccessTokenProvider : AccessTokenProvider {
    override val token: String? = null

    override suspend fun refresh(): String? = null
}

