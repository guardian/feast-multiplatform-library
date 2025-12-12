package com.gu.recipe

import com.gu.recipe.unit.Unit

data class Amount(
    val min: Float,
    val max: Float? = null,
    val unit: Unit? = null,
    val usCust: Boolean? = null,
)
