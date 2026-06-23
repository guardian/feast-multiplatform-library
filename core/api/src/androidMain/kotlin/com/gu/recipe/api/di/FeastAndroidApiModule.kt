package com.gu.recipe.api.di

import android.content.Context
import com.gu.recipe.api.config.FeastApiConfig
import com.gu.recipe.api.config.toGraphQlConfig
import com.gu.recipe.api.repository.GraphQlRecipeRepository
import com.gu.recipe.api.repository.RecipeRepository
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
    single<RecipeRepository> { GraphQlRecipeRepository(get()) }
}

fun androidFeastApiModule(
    context: Context,
    config: FeastApiConfig,
): Module = module {
    includes(androidGraphQlModule(context, config.toGraphQlConfig()))
    single<RecipeRepository> { GraphQlRecipeRepository(get()) }
}

