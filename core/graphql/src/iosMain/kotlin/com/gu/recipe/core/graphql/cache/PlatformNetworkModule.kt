package com.gu.recipe.core.graphql.cache

import com.apollographql.cache.normalized.api.NormalizedCacheFactory
import com.apollographql.cache.normalized.memory.MemoryCacheFactory
import org.koin.dsl.module

val platformNetworkModule = module {
    single<NormalizedCacheFactory> {
        //SqlNormalizedCacheFactory(name = "feast_graphql.db")
        MemoryCacheFactory()
    }
}
