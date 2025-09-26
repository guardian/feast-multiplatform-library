package com.gu.recipe

import kotlinx.serialization.json.*
import kotlinx.serialization.Serializable

data class Template(val value: String)

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

data class ParsedTemplate(
    val elements: List<TemplateElement>
)

fun parseTemplate(template: Template): ParsedTemplate {
    val pattern = Regex("""\{(?:[^{}"]|"(?:[^"\\\\]|\\\\.)*")*\}""")
    val parts = mutableListOf<TemplateElement>()
    var lastEnd = 0
    val templateText = template.value

    pattern.findAll(templateText).forEach { match ->
        // Add text before JSON
        if (match.range.first > lastEnd) {
            val textPart = templateText.substring(lastEnd, match.range.first)
            if (textPart.isNotBlank()) {
                parts.add(TemplateElement.TemplateConst(textPart))
            }
        }

        // Try to parse JSON
        try {
            val jsonObj = Json.parseToJsonElement(match.value).jsonObject
            if (jsonObj.keys.contains("min")) {
                Json.decodeFromJsonElement<TemplateElement.QuantityPlaceholder>(jsonObj).let {
                    parts.add(it)
                }
            } else {
                Json.decodeFromJsonElement<TemplateElement.OvenTemperaturePlaceholder>(jsonObj).let {
                    parts.add(it)
                }
            }
        } catch (e: Exception) {
            print("Failed to parse JSON: ${match.value}, error: ${e.message}")
            parts.add(TemplateElement.TemplateConst(match.value))
        }

        lastEnd = match.range.last + 1
    }

    // Add remaining text
    if (lastEnd < templateText.length) {
        val remaining = templateText.substring(lastEnd)
        if (remaining.isNotBlank()) {
            parts.add(TemplateElement.TemplateConst(remaining))
        }
    }

    return ParsedTemplate(parts)
}

fun scaleTemplate(template: ParsedTemplate, factor: Float): String {
    val scaledParts = template.elements.map { element ->
        when (element) {
            is TemplateElement.TemplateConst -> element.value
            is TemplateElement.QuantityPlaceholder -> {
                val (scaledMin, scaledMax) = if (element.scale) {
                    val scaledMin = (element.min * factor)
                    val scaledMax = element.max?.let { (it * factor) }
                    Pair(scaledMin, scaledMax)
                } else {
                    Pair(element.min, element.max)
                }
                val unit = element.unit ?: ""
                // TODO, we'll probably need to decide if we want to round the values depending on the unit
                if (scaledMax != null) {
                    "${scaledMin.toInt()}-${scaledMax.toInt()} $unit"
                } else {
                    "${scaledMin.toInt()} $unit"
                }
            }
            is TemplateElement.OvenTemperaturePlaceholder -> {
                val tempC = element.temperatureC
                val tempFanC = element.temperatureFanC
                "${tempC}°C${tempFanC?.let { " (${it}°C fan)" } ?: ""}"
            }
        }
    }

    return scaledParts.joinToString("")
}

data class RecipeTemplate(
    val id: String,
    val title: String,
    val ingredients: List<Template>,
    val instructions: List<Template>
)

data class Recipe(
    val id: String,
    val title: String,
    val ingredients: List<String>,
    val instructions: List<String>
)

fun scaleRecipe(recipe: RecipeTemplate, factor: Float): Recipe {
    val scaledIngredients = recipe.ingredients.map { it -> scaleTemplate(parseTemplate(it), factor) }
    val scaledInstructions = recipe.instructions.map { it -> scaleTemplate(parseTemplate(it), factor) }

    return Recipe(
        id = recipe.id,
        title = recipe.title,
        ingredients = scaledIngredients,
        instructions = scaledInstructions
    )
}