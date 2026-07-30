package com.gu.recipe.api.di

import android.content.Context
import com.gu.recipe.api.repository.GraphQLRepository
import com.gu.recipe.api.repository.GraphQlRepositoryImpl
import com.gu.recipe.core.graphql.config.GraphQlConfig
import com.gu.recipe.core.graphql.di.graphQlModule
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module
import org.koin.dsl.module

fun androidFeastApiModule(
    baseUrl: String,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
): Module = module {
    includes(graphQlModule(GraphQlConfig(baseUrl = baseUrl), ioDispatcher))
    single<GraphQLRepository> { GraphQlRepositoryImpl(get()) }
}

@Suppress("UNUSED_PARAMETER")
fun androidFeastApiModule(
    context: Context,
    baseUrl: String,
): Module = androidFeastApiModule(baseUrl)

