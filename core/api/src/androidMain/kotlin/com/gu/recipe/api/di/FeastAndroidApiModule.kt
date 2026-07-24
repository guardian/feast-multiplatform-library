package com.gu.recipe.api.di

import android.content.Context
import com.gu.recipe.api.config.FeastApiConfig
import com.gu.recipe.api.config.toGraphQlConfig
import com.gu.recipe.api.repository.GraphQlRepositoryImpl
import com.gu.recipe.api.repository.GraphQLRepository
import com.gu.recipe.core.graphql.androidGraphQlModule
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module
import org.koin.dsl.module

fun androidFeastApiModule(
    config: FeastApiConfig,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
): Module = module {
    includes(androidGraphQlModule(config.toGraphQlConfig(), ioDispatcher))
    single<GraphQLRepository> { GraphQlRepositoryImpl(get()) }
}

fun androidFeastApiModule(
    context: Context,
    config: FeastApiConfig,
): Module = module {
    includes(androidGraphQlModule(context, config.toGraphQlConfig()))
    single<GraphQLRepository> { GraphQlRepositoryImpl(get()) }
}

