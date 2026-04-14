package com.gu.recipe.loader

import kotlinx.serialization.Serializable

@Serializable
internal data class DensityCacheEntry(
    val lastModified: String,
    val content: String
)
