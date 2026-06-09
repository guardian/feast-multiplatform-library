package com.gu.recipe.core.networking

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

internal object OkHttpClientFactory {
    fun create(config: NetworkConfig): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            config.logger.log(message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        return OkHttpClient.Builder()
            .connectTimeout(config.connectTimeout.inWholeMilliseconds, TimeUnit.MILLISECONDS)
            .readTimeout(config.readTimeout.inWholeMilliseconds, TimeUnit.MILLISECONDS)
            .writeTimeout(config.writeTimeout.inWholeMilliseconds, TimeUnit.MILLISECONDS)
            .addInterceptor(AuthorizationTokenInterceptor(config.accessTokenProvider))
            .addInterceptor(loggingInterceptor)
            .build()
    }
}

