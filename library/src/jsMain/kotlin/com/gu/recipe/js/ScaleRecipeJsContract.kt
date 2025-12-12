package com.gu.recipe.js

import com.gu.recipe.unit.MeasuringSystem
import com.gu.recipe.generated.RecipeV3
import com.gu.recipe.template.ParsedTemplate
import com.gu.recipe.template.TemplateElement
import kotlinx.serialization.json.Json

private val tolerantJson = Json { ignoreUnknownKeys = true }

@OptIn(ExperimentalJsExport::class)
@JsExport
fun scaleRecipe(recipe: String, factor: Float, unit: String): String {
    val parsedRecipe = tolerantJson.decodeFromString<RecipeV3>(recipe)
    val measuringSystem = when (unit) {
        "Imperial" -> MeasuringSystem.Imperial
        "Metric" -> MeasuringSystem.Metric
        else -> throw IllegalArgumentException("Unknown unit: $unit")
    }
    val scaledRecipe = com.gu.recipe.scaleAndConvertUnitRecipe(parsedRecipe, factor, measuringSystem)
    return Json.encodeToString(scaledRecipe)
}



@OptIn(ExperimentalJsExport::class)
@JsExport
fun parseTemplate(templateString: String): List<TemplateElement> {
    val parsedTemplate = com.gu.recipe.template.parseTemplate(templateString)
    return parsedTemplate.elements
}

@OptIn(ExperimentalJsExport::class)
@JsExport
fun renderTemplate(templateElements: List<TemplateElement>): String {
    val template = ParsedTemplate(templateElements)
    return com.gu.recipe.renderTemplate(template, 1.0f, MeasuringSystem.Metric)
}