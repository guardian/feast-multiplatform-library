package com.gu.recipe.js

import com.gu.recipe.RenderSession
import com.gu.recipe.generated.OriginalMeasuringSystem
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
 * `session` is a RenderSession object which can be obtained by calling `createTemplateSession`
 */
fun renderRecipe(recipe: String, factor: Float, unit: String, session: RenderSession): String {
    val parsedRecipe = tolerantJson.decodeFromString<RecipeV3>(recipe)
    val measuringSystem = when (unit) {
        "Imperial" -> MeasuringSystem.Imperial
        "Metric" -> MeasuringSystem.Metric
        "US" -> MeasuringSystem.USCustomary
        "USWithMetric" -> MeasuringSystem.USCustomaryWithMetric
        "USWithImperial" -> MeasuringSystem.USCustomaryWithImperial
        "Combined" -> MeasuringSystem.USCombined
        else -> throw IllegalArgumentException("Unknown unit: $unit")
    }
    val scaledRecipe = session.renderRecipe(parsedRecipe, factor, measuringSystem)
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
fun renderTemplate(templateElements: List<TemplateElement>, session: RenderSession, unit: String, originalUnits: String): String {
    val measuringSystem = when (unit) {
        "Imperial" -> MeasuringSystem.Imperial
        "Metric" -> MeasuringSystem.Metric
        "US" -> MeasuringSystem.USCustomary
        "USWithMetric" -> MeasuringSystem.USCustomaryWithMetric
        "USWithImperial" -> MeasuringSystem.USCustomaryWithImperial
        "Combined" -> MeasuringSystem.USCombined
        else -> throw IllegalArgumentException("Unknown unit: $unit")
    }
    val originalMeasuringSystem = when (originalUnits) {
        "imperial" -> MeasuringSystem.Imperial
        "aus-cups" -> MeasuringSystem.Metric
        "metric" -> MeasuringSystem.Metric
        "us" -> MeasuringSystem.USCustomary
        else -> throw IllegalArgumentException("Unknown unit: $originalUnits")
    }

    val template = ParsedTemplate(templateElements)
    return session.renderTemplate(template, 1.0f, measuringSystem, originalMeasuringSystem)
}

@OptIn(ExperimentalJsExport::class)
@JsExport
/**
* JS style factory for RenderSession.  If the session can be created,
* it is returned; if the session cannot be created, then an exception is thrown.
*/
fun createTemplateSession(rawDensityData: String?, rawTerminologyData: String?):RenderSession {
    return newRenderSession(rawDensityData, rawTerminologyData).getOrThrow()
}