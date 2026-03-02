package com.gu.recipe

import com.gu.recipe.unit.MeasurementUnit

data class Amount(
    val min: Float,
    val max: Float? = null,
    val unit: MeasurementUnit? = null,
    val usCust: Boolean? = null,
    val remainderMin: Float? = null,
    val remainderMax: Float? = null,
) {
    fun asFractional(): Amount {
        return Amount(
            this.min + (this.remainderMin ?: 0f),
            this.max?.let { it + (this.remainderMax ?: 0f)},
            this.unit,
            this.usCust
        )
    }
}
