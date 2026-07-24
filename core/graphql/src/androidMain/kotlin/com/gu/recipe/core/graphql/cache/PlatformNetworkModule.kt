package com.gu.recipe.core.graphql.cache

import com.apollographql.cache.normalized.api.NormalizedCacheFactory
import com.apollographql.cache.normalized.sql.SqlNormalizedCacheFactory
import org.koin.dsl.module

val platformNetworkModule = module {
    single<NormalizedCacheFactory> {
        SqlNormalizedCacheFactory(context = get(), name = "feast_graphql.db")
    }
}