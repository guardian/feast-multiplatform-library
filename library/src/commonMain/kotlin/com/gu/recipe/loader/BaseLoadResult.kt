package com.gu.recipe.loader

sealed interface BaseLoadResult {
    data class Success(val content: String) : BaseLoadResult
    data class Failure(val reason: String? = null) : BaseLoadResult
}