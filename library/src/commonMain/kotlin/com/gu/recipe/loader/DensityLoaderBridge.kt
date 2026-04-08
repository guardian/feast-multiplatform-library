package com.gu.recipe.loader

interface DensityLoaderBridge {
    suspend fun loadDensityData(url: String, authToken: String): DensityLoadResult
}

