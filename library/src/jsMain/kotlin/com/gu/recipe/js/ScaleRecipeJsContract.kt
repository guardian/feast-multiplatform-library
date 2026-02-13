package com.gu.recipe.js

import com.gu.recipe.TemplateSession
import com.gu.recipe.unit.MeasuringSystem
import com.gu.recipe.generated.RecipeV3
import com.gu.recipe.newTemplateSession
import com.gu.recipe.template.ParsedTemplate
import com.gu.recipe.template.TemplateElement
import kotlinx.serialization.json.Json

private val tolerantJson = Json { ignoreUnknownKeys = true }

@OptIn(ExperimentalJsExport::class)
@JsExport
/**
 * Scales the given recipe by a given factor and optionally converts between measuring systems.
 * `session` is a TemplateSession object which can be obtained by calling `createTemplateSession`
 */
fun scaleRecipe(recipe: String, factor: Float, unit: String, session: TemplateSession): String {
    val parsedRecipe = tolerantJson.decodeFromString<RecipeV3>(recipe)
    val measuringSystem = when (unit) {
        "Imperial" -> MeasuringSystem.Imperial
        "Metric" -> MeasuringSystem.Metric
        "US" -> MeasuringSystem.USCustomary
        else -> throw IllegalArgumentException("Unknown unit: $unit")
    }
    val scaledRecipe = session.scaleAndConvertUnitRecipe(parsedRecipe, factor, measuringSystem)
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
fun renderTemplate(templateElements: List<TemplateElement>, session: TemplateSession): String {
    val template = ParsedTemplate(templateElements)
    return session.renderTemplate(template, 1.0f, MeasuringSystem.Metric)
}

@OptIn(ExperimentalJsExport::class)
@JsExport
/**
* JS style factory for TemplateSession.  If the session can be created,
* it is returned; if the session cannot be created, then an exception is thrown.
*/
fun createTemplateSession():TemplateSession {
    return newTemplateSession().getOrThrow()
}