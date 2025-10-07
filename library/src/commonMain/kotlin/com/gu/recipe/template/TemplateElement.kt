package com.gu.recipe.template

import kotlinx.serialization.Serializable

sealed interface TemplateElement {
    data class TemplateConst(
        val value: String
    ) : TemplateElement

    @Serializable
    data class QuantityPlaceholder(
        val min: Float,
        val max: Float? = null,
        val unit: String? = null,
        val scale: Boolean = false,
    ) : TemplateElement

    @Serializable
    data class OvenTemperaturePlaceholder(
        val temperatureC: Int,
        val temperatureFanC: Int? = null,
        val temperatureF: Int? = null,
        val gasMark: Float? = null,
    ) : TemplateElement
}