package com.gu.recipe.loader

sealed interface TerminologyLoadResult : BaseLoadResult {
   data class Success(val content: String) : TerminologyLoadResult{
        val baseResult = BaseLoadResult.Success(content)
    }

    data class Failure(val reason: String? = null) : TerminologyLoadResult {
        val baseResult = BaseLoadResult.Failure(reason)
    }
}