package com.gu.recipe.api.di

import com.gu.recipe.api.config.FeastApiConfig
import com.gu.recipe.api.config.toGraphQlConfig
import com.gu.recipe.api.repository.GraphQlRecipeRepository
import com.gu.recipe.api.repository.RecipeRepository
import com.gu.recipe.core.graphql.iosGraphQlModule
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module
import org.koin.dsl.module

fun iosFeastApiModule(
    config: FeastApiConfig,
    ioDispatcher: CoroutineDispatcher = Dispatchers.Default,
): Module = module {
    includes(iosGraphQlModule(config.toGraphQlConfig(), ioDispatcher))
    single<RecipeRepository> { GraphQlRecipeRepository(get()) }
}

