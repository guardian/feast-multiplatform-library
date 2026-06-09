package com.gu.recipe.core.graphql

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@Module
@InstallIn(SingletonComponent::class)
internal object GraphQlAndroidHiltBridgeModule {
    @Provides
    @Named(GraphQlQualifiers.IoDispatcher)
    fun provideGraphQlIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}

