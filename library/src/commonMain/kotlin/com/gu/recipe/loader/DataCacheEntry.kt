package com.gu.recipe.loader

import kotlinx.serialization.Serializable

@Serializable
internal data class DataCacheEntry(
    val lastModified: String,
    val content: String
)

