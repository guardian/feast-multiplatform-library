package com.gu.recipe.core.graphql

import com.gu.recipe.core.graphql.cache.platformNetworkModule
import com.gu.recipe.core.graphql.config.GraphQlConfig
import com.gu.recipe.core.graphql.di.graphQlModule
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module
import org.koin.dsl.module

fun iosGraphQlModule(
    config: GraphQlConfig,
    ioDispatcher: CoroutineDispatcher = Dispatchers.Default,
): Module = module {
    includes(
        platformNetworkModule,
        graphQlModule(
            config = config,
            ioDispatcher = ioDispatcher,
        ),
    )
}

