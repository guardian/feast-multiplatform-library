package com.gu.recipe.loader

interface TerminologyLoaderBridge : BaseLoaderBridge {
    suspend fun loadTerminologyData(url: String, authToken: String?): TerminologyLoadResult
}