package com.gu.recipe.core.networking

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

internal object RetrofitFactory {
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        isLenient = true
    }

    fun create(
        config: NetworkConfig,
        okHttpClient: OkHttpClient = OkHttpClientFactory.create(config),
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(config.baseUrlProvider.baseUrl())
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
}

