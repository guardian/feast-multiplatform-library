package com.gu.recipe.core.networking

import okhttp3.Interceptor
import okhttp3.Response

internal class AuthorizationTokenInterceptor(
    private val accessTokenProvider: AccessTokenProvider,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val newRequest = originalRequest.newBuilder()
            .removeHeader(AUTHORIZATION_HEADER_KEY)
            .apply {
                accessTokenProvider.token?.takeIf { it.isNotBlank() }?.let { token ->
                    header(AUTHORIZATION_HEADER_KEY, "$AUTHORIZATION_HEADER_VALUE $token")
                }
            }
            .addHeader("accept", "application/json")
            .build()

        return chain.proceed(newRequest)
    }

    internal companion object {
        /**
         * Key for the HTTP Authorization header.
         */
        const val AUTHORIZATION_HEADER_KEY = "Authorization"

        /**
         * Value prefix for the HTTP Authorization header - eg "Bearer $token".
         */
        const val AUTHORIZATION_HEADER_VALUE = "Bearer"
    }
}

