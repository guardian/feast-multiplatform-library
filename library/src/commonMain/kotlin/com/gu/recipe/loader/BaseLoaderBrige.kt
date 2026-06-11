package com.gu.recipe.loader

interface BaseLoaderBridge {
    suspend fun loadData(url: String, authToken: String?): BaseLoadResult
}