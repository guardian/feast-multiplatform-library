package com.gu.recipe.loader
sealed interface DataLoadResult {
    data class Success(val content: String) : DataLoadResult
    data class Failure(val reason: String? = null) : DataLoadResult
}

