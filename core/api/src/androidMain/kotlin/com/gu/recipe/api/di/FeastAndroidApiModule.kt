package com.gu.recipe.api.di

import android.content.Context
import com.gu.recipe.api.repository.GraphQLRepository
import com.gu.recipe.api.repository.GraphQlRepositoryImpl
import com.gu.recipe.core.graphql.androidGraphQlModule
import com.gu.recipe.core.graphql.config.GraphQlConfig
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Creates a Koin module for the Feast API with GraphQL support.
 *
 * @param baseUrl The base URL for GraphQL requests
 * @param ioDispatcher The coroutine dispatcher for IO operations (defaults to Dispatchers.IO)
 * @return A Koin Module configured with GraphQL repository
 */
fun androidFeastApiModule(
    baseUrl: String,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
): Module = module {
    includes(androidGraphQlModule(GraphQlConfig(baseUrl = baseUrl), ioDispatcher))
    single<GraphQLRepository> { GraphQlRepositoryImpl(get()) }
}

/**
 * Creates a Koin module for the Feast API with GraphQL support and Android Context.
 *
 * @param context The Android Context
 * @param baseUrl The base URL for GraphQL requests
 * @return A Koin Module configured with GraphQL repository
 */
fun androidFeastApiModule(
    context: Context,
    baseUrl: String,
): Module = module {
    includes(androidGraphQlModule(context, GraphQlConfig(baseUrl = baseUrl)))
    single<GraphQLRepository> { GraphQlRepositoryImpl(get()) }
}

