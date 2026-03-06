package com.gu.recipe.js

import com.gu.recipe.Loader
import com.gu.recipe.TemplateSession
import com.gu.recipe.unit.MeasuringSystem
import com.gu.recipe.generated.RecipeV3
import com.gu.recipe.newTemplateSession
import com.gu.recipe.template.ParsedTemplate
import com.gu.recipe.template.TemplateElement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.promise
import kotlin.js.Promise

private val tolerantJson = Json { ignoreUnknownKeys = true }
//Using this rather than MainScope apparently means there are no underlying UI assumptions or leakage risk
private val libraryScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

@OptIn(ExperimentalJsExport::class)
@JsExport
fun initialise(url: String, authToken: String): Promise<TemplateSession> {
    return libraryScope.promise {
        Loader.initialiseConversionSession(url, authToken).getOrThrow()
    }
}

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
        "USWithMetric" -> MeasuringSystem.USCustomaryWithMetric
        "USWithImperial" -> MeasuringSystem.USCustomaryWithImperial
        "Combined" -> MeasuringSystem.USCombined
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
fun renderTemplate(templateElements: List<TemplateElement>, session: TemplateSession, unit: String): String {
    val measuringSystem = when (unit) {
        "Imperial" -> MeasuringSystem.Imperial
        "Metric" -> MeasuringSystem.Metric
        "US" -> MeasuringSystem.USCustomary
        "USWithMetric" -> MeasuringSystem.USCustomaryWithMetric
        "USWithImperial" -> MeasuringSystem.USCustomaryWithImperial
        "Combined" -> MeasuringSystem.USCombined
        else -> throw IllegalArgumentException("Unknown unit: $unit")
    }
    val template = ParsedTemplate(templateElements)
    return session.renderTemplate(template, 1.0f, measuringSystem)
}

@OptIn(ExperimentalJsExport::class)
@JsExport
/**
* JS style factory for TemplateSession.  If the session can be created,
* it is returned; if the session cannot be created, then an exception is thrown.
*/
fun createTemplateSession(rawDensityData: String?):TemplateSession {
    return newTemplateSession(rawDensityData).getOrThrow()
}