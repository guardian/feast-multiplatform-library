package com.gu.recipe.core.graphql

import com.apollographql.cache.normalized.api.NormalizedCacheFactory
import com.gu.recipe.core.graphql.config.GraphQlConfig
import com.gu.recipe.core.networking.FeastNetworkApiEndpoint
import com.gu.recipe.core.networking.NetworkConfig
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

class GraphQlIosModuleTest {
    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun resolvesSqlCacheFactoryOnIos() {
        val koinApplication = startKoin {
            modules(
                iosGraphQlModule(
                    config = GraphQlConfig(
                        networkConfig = NetworkConfig(endpoint = FeastNetworkApiEndpoint.CODE),
                    ),
                ),
            )
        }

        assertNotNull(koinApplication.koin.get<NormalizedCacheFactory>())
    }
}



