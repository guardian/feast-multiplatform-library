package com.gu.recipe.core.graphql

import android.content.Context
import com.gu.recipe.core.graphql.config.GraphQlConfig
import com.gu.recipe.core.graphql.di.GraphQlQualifiers
import com.gu.recipe.core.graphql.di.graphQlModule
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module

fun androidGraphQlModule(
    config: GraphQlConfig,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
): Module = graphQlModule(
    config = config,
    ioDispatcher = ioDispatcher,
)

fun androidGraphQlModule(
    context: Context,
    config: GraphQlConfig,
): Module = graphQlModule(
    config = config,
    ioDispatcher = EntryPointAccessors.fromApplication(
        context.applicationContext,
        GraphQlAndroidDispatcherEntryPoint::class.java,
    ).graphQlIoDispatcher(),
)

@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface GraphQlAndroidDispatcherEntryPoint {
    @javax.inject.Named(GraphQlQualifiers.IoDispatcher)
    fun graphQlIoDispatcher(): CoroutineDispatcher
}

