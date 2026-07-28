package com.gu.recipe.api.di

import com.gu.recipe.api.repository.GraphQlRepositoryImpl
import com.gu.recipe.api.repository.GraphQLRepository
import com.gu.recipe.core.graphql.iosGraphQlModule
import com.gu.recipe.core.graphql.config.GraphQlConfig
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module
import org.koin.dsl.module

fun iosFeastApiModule(
    baseUrl: String,
    ioDispatcher: CoroutineDispatcher = Dispatchers.Default,
): Module = module {
    includes(iosGraphQlModule(GraphQlConfig(baseUrl = baseUrl), ioDispatcher))
    single<GraphQLRepository> { GraphQlRepositoryImpl(get()) }
}

