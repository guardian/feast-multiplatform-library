package com.gu.recipe.template

import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport
sealed interface TemplateElement

@OptIn(ExperimentalJsExport::class)
@JsExport
data class TemplateConst(
    val value: String
) : TemplateElement

@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class QuantityPlaceholder(
    val min: Float,
    val max: Float? = null,
    val unit: String? = null,
    val scale: Boolean = false,
) : TemplateElement

@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class OvenTemperaturePlaceholder(
    val temperatureC: Int? = null,
    val temperatureFanC: Int? = null,
    val temperatureF: Int? = null,
    val gasMark: Float? = null,
) : TemplateElement