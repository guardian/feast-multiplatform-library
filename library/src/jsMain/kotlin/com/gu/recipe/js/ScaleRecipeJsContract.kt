package com.gu.recipe.js

import com.gu.recipe.IngredientUnit
import com.gu.recipe.ServerSideRecipe
import kotlinx.serialization.json.Json

private val tolerantJson = Json { ignoreUnknownKeys = true }

@OptIn(ExperimentalJsExport::class)
@JsExport
fun scaleRecipe(recipe: String, factor: Float, unit: String): String {
    val parsedRecipe = tolerantJson.decodeFromString<ServerSideRecipe>(recipe)
    val ingredientUnit = when (unit) {
        "Imperial" -> IngredientUnit.Imperial
        "Metric" -> IngredientUnit.Metric
        else -> throw IllegalArgumentException("Unknown unit: $unit")
    }
    val scaledRecipe = com.gu.recipe.scaleRecipe(parsedRecipe, factor, ingredientUnit)
    return Json.encodeToString(scaledRecipe)
}