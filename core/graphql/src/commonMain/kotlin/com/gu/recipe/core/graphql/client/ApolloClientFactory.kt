package com.gu.recipe.core.graphql.client

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.cache.normalized.api.DefaultCacheKeyGenerator
import com.apollographql.cache.normalized.api.DefaultCacheResolver
import com.apollographql.cache.normalized.api.NormalizedCacheFactory
import com.apollographql.cache.normalized.memory.MemoryCacheFactory
import com.apollographql.cache.normalized.normalizedCache
import com.gu.recipe.core.graphql.config.GraphQlConfig
import kotlinx.coroutines.CoroutineDispatcher

class ApolloClientFactory(
    private val dispatcher: CoroutineDispatcher,
) {
    @OptIn(ApolloExperimental::class)
    fun create(config: GraphQlConfig, diskCache: NormalizedCacheFactory): ApolloClient {
        return ApolloClient.Builder()
            .serverUrl(config.serverUrl)
            .dispatcher(dispatcher)
            .failFastIfOffline(true)
            .normalizedCache(
                diskCache,
                DefaultCacheKeyGenerator,
                DefaultCacheResolver
            )
            .build()
    }
}