package com.gu.recipe.api.di

import com.gu.recipe.api.config.FeastApiConfig
import com.gu.recipe.api.config.toGraphQlConfig
import com.gu.recipe.api.repository.GraphQlRepositoryImpl
import com.gu.recipe.api.repository.GraphQLRepository
import com.gu.recipe.core.graphql.di.graphQlModule
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module
import org.koin.dsl.module

fun feastApiModule(
    config: FeastApiConfig,
    ioDispatcher: CoroutineDispatcher = Dispatchers.Default,
): Module = module {
    includes(graphQlModule(config.toGraphQlConfig(), ioDispatcher))
    single<GraphQLRepository> { GraphQlRepositoryImpl(get()) }
}

