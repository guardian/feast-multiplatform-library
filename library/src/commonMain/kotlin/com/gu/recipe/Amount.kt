package com.gu.recipe

import com.gu.recipe.unit.MeasurementUnit

data class Amount(
    val min: Float,
    val max: Float? = null,
    val unit: MeasurementUnit? = null,
    val usCust: Boolean? = null,
)
