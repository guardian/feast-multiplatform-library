package com.gu.recipe.loader
interface DataLoaderBridge {
    suspend fun loadData(url: String, authToken: String?): DataLoadResult
}

