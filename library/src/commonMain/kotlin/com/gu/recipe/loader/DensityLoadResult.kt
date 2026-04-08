package com.gu.recipe.loader

sealed interface DensityLoadResult {
    data class Success(val content: String) : DensityLoadResult
    data class Failure(val reason: String? = null) : DensityLoadResult
}

